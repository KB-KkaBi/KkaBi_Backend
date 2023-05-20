package com.project.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.domain.AccountList;
import com.project.domain.AccountLog;
import com.project.repository.AccountListRepository;
import com.project.repository.AccountLogRepository;

@Service
@Transactional
@EnableScheduling
public class AccountLogServiceImpl implements AccountLogService{
	
	@Autowired
	private AccountLogRepository accountLogRep;
	
	@Autowired
	private AccountListRepository accountListRep;

	@Override
	public AccountLog insertAccountLog(AccountLog accountLog) {
		
		AccountLog accountLogNew = accountLogRep.save(accountLog);
		
		accountListRep.updateAccountMoney(accountLog.getAccountLogMoney(), accountLog.getAccountList().getAccountId());
		
		return accountLogNew;
	}
	

	@Override
	public List<AccountLog> selectAccoungLog(int accountId) {
		
		List<AccountLog> accountLogList = accountLogRep.findAllByAccountListJPQL(accountId);
		
		return accountLogList;
	}

	
	@Override
	@Scheduled(cron = "0 0 0 * * *")
	public void insertAccountLogForInterest() {
		
		// 모든 계좌를 가져온다.
		List<AccountList> accountList = accountListRep.findAll();
		
		if(accountList != null) {
			for(AccountList l : accountList) {
				
				String accountType = l.getAccountInfo().getAccountType();
				String status = l.getStatus();
				int dueDate = l.getAccountInfo().getDueDate();
				LocalDate current = LocalDate.now();
				
				// 2. 내 계좌가 활성화 되어있다면
				if(status.equals("1")) {
					// 3. 적금이라면
					if(accountType.contains("적금")) {
						// 4. 현재 날짜가 적금만기일 다음이라면
						if(current.isAfter(l.getCreatedAt().toLocalDate().plusDays(dueDate))) {
							
							// 내 계좌 업데이트
							int interest =(int)(l.getAccountMoney() * (l.getAccountInfo().getInterestRate()));
							l.setAccountMoney(l.getAccountMoney() + interest);
							l.setStatus("2");
							
							// 계좌 로그 기록
							accountLogRep.save(AccountLog.builder()
									.accountList(l)
									.accountLogMoney(l.getAccountMoney() + interest)
									.transactionAmount(interest)
									.transactionReason("만기 이자")
									.transactionType("이자")
									.build());
						}
					}
				}
				
			}
		}
	}
	
	

}
