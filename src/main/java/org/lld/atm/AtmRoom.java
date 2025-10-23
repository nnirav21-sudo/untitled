package org.lld.atm;

import lombok.AllArgsConstructor;
import lombok.Data;


public class AtmRoom {
    Atm atm;
    User user;


    @Data
    static class Atm{
        private static Atm atm = new Atm();
        AtmState currentState;
        String id;
        private int amount;
        int noOf2kNotes;
        int noOf500Notes;
        int noOf100Notes;

        public void setAtmBalance(int amount,int noOf2kNotes,int noOf500Notes,int noOf100Notes){
            this.amount = amount;
            this.noOf2kNotes = noOf2kNotes;
            this.noOf500Notes = noOf500Notes;
            this.noOf100Notes = noOf100Notes;
        }
        public void deDuctTwoThousandNotes(int no){
            this.noOf2kNotes = noOf2kNotes-no;
        }
        public void deDuctFiveHundredNotes(int no){
            this.noOf500Notes = noOf500Notes-no;
        }
        public void deDuctHundredNotes(int no){
            this.noOf100Notes = noOf100Notes-no;
        }
        public void deDuctAmount(int amount){
            this.amount = amount;
        }
        public static Atm getAtmObject(){
            atm.setCurrentState(new IdleState());
            return atm;
        }

        public void printCurrentATMStatus(){
            System.out.println("Balance: " + amount);
            System.out.println("2kNotes: " + noOf2kNotes);
            System.out.println("500Notes: " + noOf100Notes);
            System.out.println("100Notes: " + noOf100Notes);

        }
    }
    @Data
    @AllArgsConstructor
    static class BankAccount{
        String bankAccountNumber;
        String Ifsc;
        private int bankBalance;
        public void deDuctAmount(int amount){
            this.bankBalance = bankBalance-amount;
        }
    }

    @Data
    @AllArgsConstructor
    static class Card{
        String cardNumber;
        BankAccount bankAccount;
        String userName;
        String pin;
        public void deDuctBankBalance(int amount){
            this.bankAccount.deDuctAmount(amount);
        }
        //other metadata
    }
    @Data
    @AllArgsConstructor
    static class User{
        String id;
        Card card;
    }
    static enum Transaction_type{
        WITHDRAWAL,CHECK_BALANCE;

        public static void showOperation(){
            for(Transaction_type t:Transaction_type.values()){
                System.out.println(t);
            }
        }
    }

    static abstract class AtmState{
        public void insertCard(Atm atm,Card card) throws Exception{
            throw new UnsupportedOperationException("Not supported yet.");
        }
        public void authenticateCard(Atm atm ,Card card, String atmPin) throws Exception{
            throw new Exception("Something went wrong");
        }
        public void selectOperation(Atm atm,Card card,String transaction_type) throws Exception{
            throw new Exception("something went wrong");
        }
        public  void withdrawCash(Atm atm,Card card,int amount) throws Exception{
            throw new Exception("something went wrong");
        }
        public void checkBalance(Atm atm,Card card) throws Exception{
            throw new Exception("something went wrong");
        }
        public void exit(Atm atm) throws Exception{
            throw new Exception("something went wrong");
        }
        public void returnCard() throws Exception{
            throw new Exception("something went wrong");
        }

    }
    static abstract class CashWithdrawalProcessor{
        CashWithdrawalProcessor nextCashWithdrawalProcessor;
        CashWithdrawalProcessor(CashWithdrawalProcessor cashWithdrawalProcessor){
            this.nextCashWithdrawalProcessor = cashWithdrawalProcessor;
        }
        public void WithdrawCash(Atm atm,int amount) {
            if(nextCashWithdrawalProcessor != null){
                nextCashWithdrawalProcessor.WithdrawCash(atm,amount);
            }
        }
    }
    static class TwoThousandCashWithdrawalProcessor extends CashWithdrawalProcessor{
        public TwoThousandCashWithdrawalProcessor(CashWithdrawalProcessor cashWithdrawalProcessor) {
            super(cashWithdrawalProcessor);
        }
        @Override
        public void WithdrawCash(Atm atm,int amount) {
            int required= amount/2000;
            int balance = amount%2000;
            if(required<= atm.getNoOf2kNotes()) {
                atm.deDuctTwoThousandNotes(required);
            }else {
                atm.deDuctTwoThousandNotes(atm.getNoOf2kNotes());
                balance = balance + (required-atm.getNoOf2kNotes()*2000);
            }
            if(balance!=0){
                super.WithdrawCash(atm,balance);
            }
        }
    }
    static class FiveHundredCashWithdrawalProcessor extends CashWithdrawalProcessor{
        public FiveHundredCashWithdrawalProcessor(CashWithdrawalProcessor cashWithdrawalProcessor) {
            super(cashWithdrawalProcessor);
        }
        @Override
        public void WithdrawCash(Atm atm,int amount) {
            int required= amount/500;
            int balance = amount%500;
            if(required<= atm.getNoOf2kNotes()) {
                atm.deDuctFiveHundredNotes(required);
            }else {
                atm.deDuctFiveHundredNotes(atm.getNoOf2kNotes());
                balance = balance + (required-atm.getNoOf2kNotes()*500);
            }
            if(balance!=0){
                super.WithdrawCash(atm,balance);
            }
        }
    }
    static class HundredCashWithdrawalProcessor extends CashWithdrawalProcessor{
        public HundredCashWithdrawalProcessor(CashWithdrawalProcessor cashWithdrawalProcessor) {
            super(cashWithdrawalProcessor);
        }
        @Override
        public void WithdrawCash(Atm atm,int amount) {
            int required= amount/100;
            int balance = amount%100;
            if(required<= atm.getNoOf2kNotes()) {
                atm.deDuctHundredNotes(required);
            }else {
                atm.deDuctHundredNotes(atm.getNoOf2kNotes());
                balance = balance + (required-atm.getNoOf2kNotes()*100);
            }
            if(balance!=0){
                super.WithdrawCash(atm,balance);
            }
        }
    }

    static class IdleState extends AtmState{
        @Override
        public void insertCard(Atm atm,Card card){
            System.out.println("Card inserted by User:"+ card.getUserName());
            atm.setCurrentState(new HasCardState());
        }
    }

    static class HasCardState extends AtmState{
        public HasCardState(){
            System.out.println("enter your card pin number");
        }

        @Override
        public void authenticateCard(Atm atm, Card card, String atmPin) throws Exception{
            System.out.println("Authenticating user:"+ card.getUserName());
            if(!card.getUserName().equals(atmPin)){
                atm.setCurrentState(new IdleState());
                exit(atm);
            }
            System.out.println("Card Authenticated successfully for user:"+card.getUserName());
            atm.setCurrentState(new SelectOperationState());
        }
        @Override
        public void exit(Atm atm) throws Exception{
            returnCard();
            atm.setCurrentState(new IdleState());
            System.out.println("Card has been exited");

        }
        public void returnCard() throws Exception{
           System.out.println("Card has been exited");
        }
    }
    static class SelectOperationState extends AtmState{
        public SelectOperationState(){
           showOperations();
        }
        @Override
        public void selectOperation(Atm atm,Card card,String transaction_type) throws Exception{
            switch (transaction_type){
                case "WITHDRAWAL": atm.setCurrentState(new CashWithdrawalState());
                break;
                case "CHECK_BALANCE": atm.setCurrentState(new CheckBalanceState());
                break;
                default:
                    System.out.println("Please select correct transaction type");
                    exit(atm);
            }
        }
        @Override
        public void exit(Atm atm) throws Exception{
            returnCard();
            atm.setCurrentState(new IdleState());
            System.out.println("Card has been exited");

        }
        public void returnCard() throws Exception{
            System.out.println("Card has been exited");
        }

        private void showOperations(){
            System.out.println("Please select the Operation");
            Transaction_type.showOperation();
        }

    }
    static class CashWithdrawalState extends AtmState{
       CashWithdrawalProcessor cashWithdrawalProcessor;
       CashWithdrawalState(){
           System.out.println("enter amount to withdraw");
          }

       @Override
        public void withdrawCash(Atm atm,Card card,int amount) throws Exception{
           if(amount>atm.amount){
               System.out.println("Amount cannot be withdrawal, Please try again");
               exit(atm);
           }
           if(card.getBankAccount().getBankBalance()<amount){
               System.out.println("insufficient balance to withdraw");
               exit(atm);
           }
           card.deDuctBankBalance(amount);
           atm.deDuctAmount(amount);
           cashWithdrawalProcessor = new TwoThousandCashWithdrawalProcessor(new FiveHundredCashWithdrawalProcessor(new HundredCashWithdrawalProcessor(null)));
           cashWithdrawalProcessor.WithdrawCash(atm,amount);

           System.out.println("Cash withdrawal successfully for user:"+card.getUserName()+" amount :"+amount);
           atm.setCurrentState(new IdleState());
           exit(atm);
       }
        @Override
        public void exit(Atm atm) throws Exception{
            returnCard();
            atm.setCurrentState(new IdleState());
            System.out.println("Card has been exited");

        }
        public void returnCard() throws Exception{
            System.out.println("Card has been exited");
        }
    }
    static class CheckBalanceState extends AtmState{
        public CheckBalanceState() {
        }

        @Override
        public void checkBalance(Atm atm, Card card){
            System.out.println("Your Balance is: " + card.getBankAccount().getBankBalance());
            exit(atm);
        }

        @Override
        public void exit(Atm atmObject){
            returnCard();
            atmObject.setCurrentState(new IdleState());
            System.out.println("Exit happens");
        }

        @Override
        public void returnCard(){
            System.out.println("Please collect your card");
        }

    }


    public static void main(String args[]) {

        AtmRoom atmRoom = new AtmRoom();
        atmRoom.initialize();
    try {
        atmRoom.atm.printCurrentATMStatus();
        atmRoom.atm.getCurrentState().insertCard(atmRoom.atm, atmRoom.user.card);
        atmRoom.atm.getCurrentState().authenticateCard(atmRoom.atm, atmRoom.user.card, "112211");
        atmRoom.atm.getCurrentState().selectOperation(atmRoom.atm, atmRoom.user.card, Transaction_type.WITHDRAWAL.name());
        atmRoom.atm.getCurrentState().withdrawCash(atmRoom.atm, atmRoom.user.card, 2700);
        atmRoom.atm.printCurrentATMStatus();
    } catch (Exception e) {
        throw new RuntimeException(e);
    }

    }

    private void initialize() {

        //create ATM
        atm = Atm.getAtmObject();
        atm.setAtmBalance(3500, 1,2,5);

        //create User
        this.user = createUser();
    }

    private User createUser(){

        return new User("RAJ",createCard());
    }

    private Card createCard(){

       return new Card("1234",createBankAccount(),"RAJ","112211");

    }

    private BankAccount createBankAccount() {

        return new BankAccount("ABCD","II00",3000);

    }

}
