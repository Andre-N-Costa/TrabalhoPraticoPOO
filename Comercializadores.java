import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.text.DecimalFormat;

public class Comercializadores implements Serializable{
    private double totalFaturado;
    private String com; // nome do comercializador
    private static final double cBase = 0.1;
    private static final double impostos = 0.3;
    private Function2<SmartDevice,CasaInteligente,Double> funcao;
    private List<Function2<SmartDevice,CasaInteligente,Double>> funcoes = new ArrayList<Function2<SmartDevice,CasaInteligente,Double>> ();
    private Map<String, Double> faturas;
    
    public Comercializadores(){
        faturas = new HashMap<>();
        this.com = "não identificado"; 
    }
    
    public Comercializadores(String com){
        this.com = com;
        faturas = new HashMap<>();
        this.totalFaturado = 0;
        Random rand = new Random();
        funcoes.add((Function2<SmartDevice,CasaInteligente,Double> & Serializable)(x,y) -> x.ConsumoDiario() * this.cBase * (1 + this.impostos) * 0.9);
        funcoes.add((Function2<SmartDevice,CasaInteligente,Double> & Serializable)(x,y) -> y.getDevices().size() > 10?x.ConsumoDiario() * this.cBase * (1 + this.impostos) * 0.7: x.ConsumoDiario() * this.cBase * (1 + this.impostos) * 0.9);
        funcoes.add((Function2<SmartDevice,CasaInteligente,Double> & Serializable)(x,y) -> y.getDevices().size() > 5?x.ConsumoDiario() * this.cBase * (1 + this.impostos) * 0.8: x.ConsumoDiario() * this.cBase * (1 + this.impostos) * 0.9);
        funcoes.add((Function2<SmartDevice,CasaInteligente,Double> & Serializable)(x,y) -> y.getDevices().size() > 15?x.ConsumoDiario() * this.cBase * (1 + this.impostos) * 0.6: x.ConsumoDiario() * this.cBase * (1 + this.impostos) * 0.9);
        funcoes.add((Function2<SmartDevice,CasaInteligente,Double> & Serializable)(x,y) -> y.getDevices().size() > 7?x.ConsumoDiario() * this.cBase * (1 + this.impostos) * 0.8: x.ConsumoDiario() * this.cBase * (1 + this.impostos) * 0.9);
        funcao = funcoes.get(rand.nextInt(5));
    }
    
    public void setTotalFaturado(double i){
        this.totalFaturado = i;
    }
    
    public void setCom(String c) {
        this.com = c;
    }
    
    public double getTotalFaturado(){
        return this.totalFaturado;
    }
    
    public String getCom1(){
        return this.com;
    }
    
    public Function2<SmartDevice,CasaInteligente,Double> getFuncao(){
        return this.funcao;
    }
    
    public void addFat(String cidate, double d){
        faturas.put(cidate,d);
    }
    
    public String faturasforn(){
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.##");
        for (Map.Entry<String,Double> fatura : faturas.entrySet()){
           sb.append("\n------------\n").append("Data e Casa, ").append(fatura.getKey())
             .append("\nValor: ").append(df.format(fatura.getValue())).append("€");
        }
        return sb.toString();
    }
    
    public boolean equals(Object o){
        if(this == o) return true;
        if((o == null) || (this.getClass() != o.getClass())) return false;
        Comercializadores e = (Comercializadores) o;
        return (e.com == this.com);
    }
    
    public Comercializadores clone(){
        return new Comercializadores(this.com);
    }
       
    public double PrecoDiaPorDisp(SmartDevice s,CasaInteligente c){
        return this.funcao.apply(s,c);
    }
}
