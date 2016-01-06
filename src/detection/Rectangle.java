package detection;

public class Rectangle {

    public int x;
    public int width;
    public int y;
    public int height;
    public float probability;

    public Rectangle(int i, int j, int width, int height) {
        this(i, j, width, height, 1f);
    }
    
    public Rectangle(int i, int j, int width, int height, float probability) {
        x = i;
        y = j;
        this.width = width;
        this.height = height;
        this.probability = probability;
    }

}
