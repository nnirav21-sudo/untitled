package nishant.lld;

class Box {
    double width;
    double height;
    double depth;

    double volume(){
        return width*height*depth;
    }
    void setDim(double h,double w,double d ){
        width=h;
        height=w;
        depth=d;
    }

    // example for method with no parameter parameter and return type
//    double getWidth(){
//        return width;
//    }
//    void setWidth(double newWidth){
//        width = newWidth;
//    }
}

public class Demo{
    public static void main(String[] args) {
        Box myBox = new Box();
        myBox.depth=10.0;
        myBox.width=20.0;
        myBox.height=30.0;
        Box box1 = new Box();
        box1.depth=10.0;
        box1.width=20.0;
        box1.height=3.0;
        System.out.println("volume of myBox is:"+myBox.volume());
        System.out.println("volume of Box1 is:"+box1.volume());


    }

}