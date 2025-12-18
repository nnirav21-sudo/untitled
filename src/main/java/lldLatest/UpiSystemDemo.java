package lldLatest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UpiSystemDemo {
    public static void main(String[] args){
        UpiPaymentService service = new UpiPaymentService();
        service.createUser("nishant", new BigDecimal("1000"));
        service.createUser("lokesh", new BigDecimal("100"));
        TxnResult t1 = service.sendMoney("req-1","nishant","lokesh", new BigDecimal(100));
        TxnResult t2 = service.sendMoney("req-1","nishant","lokesh", new BigDecimal(100));


        System.out.println(t1);
        System.out.println(t2);
        System.out.println("nishant balance: " + service.getBalance("nishant"));
        System.out.println("lokesh balance: " + service.getBalance("lokesh"));

        service.rollBack(t1.txnId);

        System.out.println("After rollback:");
        System.out.println("nishant balance: " + service.getBalance("nishant"));
        System.out.println("lokesh balance: " + service.getBalance("lokesh"));

    }}

class Account{
    final String userId;
    BigDecimal balance;
    final Lock lock = new ReentrantLock();

    Account(String userId,BigDecimal balance){
        this.userId = userId;
        this.balance = balance;
    }
}
enum Status{
    COMMITED,FAILED,ROLLED_BACK
        }


class Transaction{

    final String txnId;
    final String requestId;
    final String to;
    final String from;
    final BigDecimal amount;
    final Instant timestamp;
    Status status;
    final String message;

    Transaction(String txnId,String requestId,String to,String from,BigDecimal amount,Status status,String message){
        this.txnId = txnId;
        this.requestId = requestId;
        this.to = to;
        this.from = from;
        this.amount = amount;
        this.timestamp = Instant.now();
        this.status = status;
        this.message = message;
    }

}

class TxnResult{
    final String txnId;
    final Status status;
    final String message;
    TxnResult(String txnId,Status status,String message){
        this.txnId = txnId;
        this.status = status;
        this.message = message;
    }

    @Override
    public String toString(){
        return "TxnResult{ txId = ' " + txnId + "',status =" + status + ",message ='" + message +"'}";
    }
}

class UpiPaymentService{
    private final Map<String,Account> accounts = new ConcurrentHashMap<>();
    private final Map<String,Transaction> transactions = new ConcurrentHashMap<>();
    private final Map<String,TxnResult> txnResults = new ConcurrentHashMap<>();
    private final Queue<Transaction> transactionLog = new ConcurrentLinkedQueue<>();
    private final AtomicLong txnSequence = new AtomicLong(1);

    public void createUser(String userId,BigDecimal balance){
        if(balance.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Initial balance must be greater than zero");
        }
        accounts.putIfAbsent(userId, new Account(userId,balance));
    }
    public BigDecimal getBalance(String userId){
        Account account = accounts.get(userId);
        account.lock.lock();
        try{
            return account.balance;
        } finally{
            account.lock.unlock();

        }
    }

    public TxnResult sendMoney(String requestId, String from , String to, BigDecimal amount){
                if(requestId != null){
                    return txnResults.computeIfAbsent(requestId,
                            id -> processTransfer(id,from,to,amount)
                    );
                }
                return processTransfer(null,from,to,amount);
    }

    private TxnResult processTransfer(String requestId,String from,String to , BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount should be greater than ZERO ");
        }
        Account sender = getAccount(from);
        Account receiver = getAccount(to);

        Account first = sender.userId.compareTo(receiver.userId)<0?sender:receiver;
        Account second = first == sender? receiver : sender;

        first.lock.lock();
        second.lock.lock();

        try {
            String txnId = "TX-" + txnSequence.getAndIncrement();
            if (sender.balance.compareTo(amount) < 0) {
                Transaction txn = new Transaction(txnId,requestId, to, from, amount , Status.FAILED,"Insuffcient Balance");
                transactions.put(txnId,txn);
                transactionLog.add(txn);
                return new TxnResult(txnId,Status.FAILED,"Insufficient Balance");
            }
            sender.balance = sender.balance.subtract(amount);
            receiver.balance = receiver.balance.add(amount);

            Transaction txn = new Transaction(txnId,requestId,to,from,amount,Status.COMMITED,"transfer successful");
            transactions.put(txnId,txn);
            transactionLog.add(txn);

            return new TxnResult(txnId,Status.COMMITED,"Transfer Successfull");

        } finally{
            second.lock.unlock();
            first.lock.unlock();

        }
    }

    public TxnResult rollBack(String txnId){
        Transaction txn = transactions.get(txnId);
        if(txn == null){
            return new TxnResult(txnId,Status.FAILED,"Transaction Not Found");

        }
        if(txn.status != Status.COMMITED){
            return new TxnResult(txnId,Status.FAILED,"RollBack not Allowed");
        }

        Account sender = getAccount(txn.from);
        Account receiver = getAccount(txn.to);

        Account first = sender.userId.compareTo(receiver.userId)<0?sender:receiver;
        Account second = first == sender ? receiver : sender;

        first.lock.lock();
        second.lock.lock();
        try{
            receiver.balance = receiver.balance.subtract(txn.amount);
            sender.balance = sender.balance.add(txn.amount);
            txn.status   = Status.ROLLED_BACK;
            return new TxnResult(txnId,Status.ROLLED_BACK,"ROLL_BACK Complete");
        }
        finally{
            second.lock.unlock();
            first.lock.unlock();
        }
    }

    private Account getAccount(String userId){
        Account acc = accounts.get(userId);
        if(acc == null){
            throw new IllegalArgumentException("Account doesnot Exists");

        }
        return acc;
    }
}
