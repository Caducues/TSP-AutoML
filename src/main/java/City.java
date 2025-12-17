public class City {
    int id;
    double x;
    double y;

    public City(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    // İki şehir arası mesafe (Öklid)
    public double distanceTo(City city) {
        double xDistance = Math.abs(getX() - city.getX());
        double yDistance = Math.abs(getY() - city.getY());
        return Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));
    }

    public double getX() { return x; }
    public double getY() { return y; }

    @Override
    public String toString() { return id + ""; }
}