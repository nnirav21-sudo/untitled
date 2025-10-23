package multithreading;


import java.util.concurrent.Semaphore;

public class PrintEvenOdd {

    private final int n;
    private final Semaphore oddTurn = new Semaphore(1);
    private final Semaphore evenTurn = new Semaphore(0);

    public PrintEvenOdd(int n) {
        this.n = n;
    }

    private void printOdd(){
        for(int i = 1; i<=n;i+=2){
            try{
                oddTurn.acquire();
                System.out.println(Thread.currentThread().getName()+" -> "+ i);
            } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    return ;
            } finally {
                evenTurn.release();
            }
        }
    }

    private void printEven() {
        for(int i = 2;i<=n;i+=2){
            try{
                evenTurn.acquire();
                System.out.println(Thread.currentThread().getName()+" -> "+i);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
                return ;
            } finally{
                oddTurn.release();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        PrintEvenOdd printEvenOdd = new PrintEvenOdd(20);

        Thread printOdd = new Thread(printEvenOdd::printOdd);
        Thread printEven = new Thread(printEvenOdd::printEven);

        printEven.start();
        printOdd.start();

        printOdd.join();
        printEven.join();

    }
}
