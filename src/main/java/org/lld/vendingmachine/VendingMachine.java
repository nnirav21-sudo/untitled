package org.lld.vendingmachine;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Optional;

public class VendingMachine {
    public static void main(String[] args){
        VendingMach vendingMach = new VendingMach();
        vendingMach.displayInvetory();
        try{
           vendingMach.beginTransaction();
           vendingMach.selectProduct("201");
           vendingMach.insertCoin(50.0);
           vendingMach.dispenseProduct();
        }catch (Exception e){
           System.out.println(e.getMessage());
        } finally {
            vendingMach.displayInvetory();
        }

    }
    static enum ProductType{
        COLD_DRINKS,
        CHOC0LATE,
        CHIPS,
        BISCUIT;
    }

    @Data
    @AllArgsConstructor
    static class Product{
        ProductType type;
        String name;
        String productCode;
        Double price;
        int quantity;

    }
    @Data
    static class VendingMach{
        public ArrayList<Product> inventory ;
        VendingMachineState currentState;
        Product selectedProduct;
        double paymentMade;
        double changeToReturn;

        public VendingMach(){
            this.currentState = new IdleState();
            this.setInventory();
        }

        public void setPaymentMade(double paymentMade) {
            this.paymentMade = paymentMade;
            this.setChangeToReturn(paymentMade-this.selectedProduct.getPrice());
        }
        void displayInvetory(){
            for(Product product:inventory){
                System.out.println(product.toString());
            }
        }
        public void beginTransaction() throws Exception{
            currentState.beginTransaction(this);
        }
        public void selectProduct(String productCode) throws Exception{
            currentState.selectProduct(this, productCode);
        }
        public void insertCoin(Double amountPaid) throws Exception{
            currentState.insertCoin(this, amountPaid);
        }
        public void dispenseProduct() throws Exception{
            currentState.dispenseProduct(this);
        }
        public void initiateRefund(double amountToRefund) throws Exception{
            System.out.println("Refunded Amount: " + changeToReturn);
            this.changeToReturn = 0.00;

        }
        void setInventory(){
            System.out.println("------------------------------------------------------------------------------------");
            System.out.println("Stocking up the vending machine...");
            ArrayList<Product> products = new ArrayList<>();
            // Shelf 1 - Soft Drinks
            products.add(new Product(ProductType.COLD_DRINKS, "Coke", "101", 70.00, 5));
            products.add(new Product(ProductType.COLD_DRINKS, "Pepsi", "102", 50.00, 5));
            products.add(new Product(ProductType.COLD_DRINKS, "Sprite", "103", 50.00, 5));
            // Shelf 2 - Chips
            products.add(new Product(ProductType.CHIPS, "Lays", "201", 20.00, 5));
            products.add(new Product(ProductType.CHIPS, "Nachos", "202", 60.00, 5));
            products.add(new Product(ProductType.CHIPS, "Pringles", "203", 50.00, 5));
            // Shelf 3 - Chocolates
            products.add(new Product(ProductType.CHOC0LATE, "Munch", "301", 20.00, 5));
            products.add(new Product(ProductType.CHOC0LATE, "Snickers", "302", 50.00, 5));
            products.add(new Product(ProductType.CHOC0LATE, "5star", "303", 35.00, 1));
            this.inventory = products;
        }


    }
    static abstract class VendingMachineState{
        public void beginTransaction(VendingMach vendingMach) throws Exception{
            throw new Exception("begin transaction failed");
        }
        public void selectProduct(VendingMach vendingMach,String productCode) throws Exception{
            throw new Exception("select product failed");
        }
        public void insertCoin(VendingMach vendingMach,Double amountPaid) throws Exception{
            throw new Exception("insert coin failed");
        }
        public void dispenseProduct(VendingMach vendingMach) throws Exception{
            throw new Exception("dispense failed");
        }

    }

    static class IdleState extends VendingMachineState{
        @Override
        public void beginTransaction(VendingMach vendingMach) throws Exception{
            System.out.println("CurrentState: " + vendingMach.getCurrentState().getClass().getSimpleName());
            System.out.println("A new Transaction has been started...");
            vendingMach.setCurrentState(new SelectionState());
        }
    }

    static class SelectionState extends VendingMachineState{
        @Override
        public void selectProduct(VendingMach vendingMach,String productCode) throws Exception{
            System.out.println("CurrentState: " + vendingMach.getCurrentState().getClass().getSimpleName());
            System.out.println("Product Selection in progress...");
            System.out.println("Product selected: " + productCode);

            Optional<Product> selectedProduct = vendingMach.inventory
                    .stream().filter(product -> product.getProductCode()
                            .equals(productCode)).findFirst();
            if(selectedProduct.isEmpty()){
                vendingMach.setCurrentState(new IdleState());
                throw new Exception("Product not found. select different product");
            }
            if (selectedProduct.get().getQuantity() == 0) { // Out of Stock
                vendingMach.setCurrentState(new IdleState());
                throw new Exception("The product is out of stock. select different product.");
            }

            vendingMach.setSelectedProduct(selectedProduct.get());
            vendingMach.setCurrentState(new CollectMoneyState());

        }
    }

    static class CollectMoneyState extends VendingMachineState{
        @Override
        public void insertCoin(VendingMach vendingMach,Double amountPaid) throws Exception{
            System.out.println("Current State: " + vendingMach.getCurrentState().getClass().getSimpleName());
            System.out.println("You Paid: " + amountPaid);
            if(amountPaid<vendingMach.getSelectedProduct().getPrice()){
                vendingMach.initiateRefund(amountPaid);
                vendingMach.setCurrentState(new IdleState());
            }
            vendingMach.setPaymentMade(amountPaid);
            vendingMach.setCurrentState(new DispenseState());
        }
    }
    static class DispenseState extends VendingMachineState{
        @Override
        public void dispenseProduct(VendingMach vendingMach) throws Exception{
            System.out.println("Current State: " + vendingMach.getCurrentState().getClass().getSimpleName());
            System.out.println("You Paid: " + vendingMach.getPaymentMade());
            System.out.println("product Dispensed: " + vendingMach.getSelectedProduct().getName());
            System.out.println("change refunded : " + vendingMach.getChangeToReturn());
            vendingMach.inventory.stream().filter(product -> product.getProductCode().equals(vendingMach.getSelectedProduct().getProductCode())).findFirst().ifPresent(
                    product -> product.setQuantity(product.getQuantity()-1));
            vendingMach.setCurrentState(new IdleState());
        }
    }

}
