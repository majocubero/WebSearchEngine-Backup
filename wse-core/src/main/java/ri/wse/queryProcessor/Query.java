package ri.wse.queryProcessor;

public class Query {

    private double freq;
    private double f;
    private double w;

    public Query() {
        this.freq = 0;
        this.f = 0;
        this.w = 0;
    }

    public double getFreq() {
        return freq;
    }

    public double getF() {
        return f;
    }

    public double getW() {
        return w;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }

    public void setF(double f) {
        this.f = f;
    }

    public void setW(double w) {
        this.w = w;
    }
}
