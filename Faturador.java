import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.Iterator;

public class Faturador implements Serializable
{
    private Map<Long,CasaInteligente> casas;
    private Map<CasaInteligente,Double> fatura;
    private Map<String,CasaInteligente> mudancaPendente;
    private Map<SmartDevice, Boolean> ligardesligar;
    private Map<String,Comercializadores> comercializadores;
    
    public Faturador(){
        this.casas = new HashMap<>();
        this.fatura = new HashMap<>();
        this.mudancaPendente = new HashMap<>();
        this.comercializadores = new HashMap<>();
        this.ligardesligar = new HashMap<>();
    }
    
    public Comercializadores getCom(String s){
        return comercializadores.get(s);
    }
    
    public CasaInteligente getCasa(long nif){
        return casas.get(nif);
    }
    
    public double getFatura(CasaInteligente ci){
        return fatura.get(ci);
    }
    
    public void addOF(SmartDevice sd, boolean b){
        if (sd != null) ligardesligar.put(sd,b);
        else throw new NullPointerException();
    }
    
    public void addCasa(CasaInteligente c){
        casas.put(c.getNifP(),c);
    }

    public void addMudanca(String s, CasaInteligente ci){
        mudancaPendente.put(s,ci);
    }
    
    public void addCom(String s, Comercializadores com){
        comercializadores.put(s,com);
    }
    
    public Map<Long,CasaInteligente> getCasas(){
        return this.casas;
    }
    
    public Map<String,CasaInteligente> getMudanca(){
        return this.mudancaPendente;
    }

    
    public void calcularfatura(String dataI,String dataD){
        long diasB = ChronoUnit.DAYS.between(LocalDate.parse(dataI),LocalDate.parse(dataD));
        for (Map.Entry<Long,CasaInteligente> entry : casas.entrySet()){
            double preco = 0;
            for (Map.Entry<String,SmartDevice> sd : ((Map<String, SmartDevice>) entry.getValue().getDevices()).entrySet()){
                preco = preco + entry.getValue().getEnergia().getFuncao().apply(sd.getValue(),entry.getValue());
            }
            fatura.put(entry.getValue(),preco * diasB);
            Comercializadores com = comercializadores.get(entry.getValue().getEnergia().getCom1());
            double bfr = com.getTotalFaturado();
            com.setTotalFaturado(bfr + (preco*diasB));
            if ((preco * diasB) > 0) com.addFat(dataI + "->" + dataD + ": " + entry.getValue().getNifP(),preco*diasB);
        }
        if (mudancaPendente.size() > 0){
            for (Map.Entry<String,CasaInteligente> listaespera : mudancaPendente.entrySet()){
                listaespera.getValue().setEnergia(comercializadores.get(listaespera.getKey()));
            }
        }
        if (ligardesligar.size() > 0){
            for (Map.Entry<SmartDevice,Boolean> listaespera : ligardesligar.entrySet()){
                listaespera.getKey().setX(listaespera.getValue());
            }
        }
    
    }
    
    public String simulacaotop5(){
        Comparator<CasaInteligente> c = (c1,c2) -> (int) (c2.ConsumoTotalDiario() - c1.ConsumoTotalDiario());
        List<CasaInteligente> cil = this.casas.values().stream().sorted(c).limit(5).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        Iterator i = cil.iterator();
        int count = 0;
        while(i.hasNext()){
            sb.append(count+1).append(" - ").append(cil.get(count).getNifP()).append(" com consumo de: ").append(cil.get(count).ConsumoTotalDiario()).append("\n");
            i.next();
            count++;
        }
        return sb.toString();
    }
    
    public String maisFcasa(){
        Comparator<CasaInteligente> c = (c1,c2) -> (int) (getFatura(c2) - getFatura(c1));
        List<CasaInteligente> cil = this.casas.values().stream().sorted(c).collect(Collectors.toList());
        CasaInteligente m = cil.get(0);
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.##");
        sb.append("------------------------------").append("\nProprietário: ").append(m.getNomeP()).append("\nNif do proprietário: ").append(m.getNifP() + "\n")
        .append("Fornecedor de energia: ").append(m.getEnergia().getCom1() + "\n");
        sb.append("------------------------------\n").append("Valor: ").append(df.format(getFatura(m))).append("€");
        return sb.toString();
    }
    
    public String maisFfornecedor(){
        Comparator<Comercializadores> c = (c1,c2) -> (int) (c2.getTotalFaturado() - c1.getTotalFaturado());
        List<Comercializadores> cl = this.comercializadores.values().stream().sorted(c).collect(Collectors.toList());
        Comercializadores m = cl.get(0);
        DecimalFormat df = new DecimalFormat("#.##");
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------").append("\nFornecedor: ").append(m.getCom1())
          .append("\nValor: ").append(df.format(m.getTotalFaturado())).append("€");
        return sb.toString();
    }
    
    public void grava(String fn) throws FileNotFoundException, IOException{
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fn));
        oos.writeObject(this);
        oos.close();
    }
    
    public static Faturador le(String fn) throws FileNotFoundException, IOException, ClassNotFoundException{
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fn));
        Faturador a = (Faturador) ois.readObject();
        ois.close();
        return a;
    }
    
    public boolean checkfatura(){
        return fatura.isEmpty();
    }
    
    public String faturaString(){
        DecimalFormat df = new DecimalFormat("#.##");
        StringBuilder sb = new StringBuilder();
        sb.append("--FATURAS--\n");
        for (Map.Entry<CasaInteligente,Double> casa : fatura.entrySet()){
            sb.append("Proprietário: ").append(casa.getKey().getNomeP()).append("\nNif do proprietário: ").append(casa.getKey().getNifP() + "\n");
            sb.append("Valor: ").append(df.format(casa.getValue())).append("€");
            sb.append("\n-----------\n");
        }
        return sb.toString();
    }
    
    public String singlefaturaString(CasaInteligente ci){
        DecimalFormat df = new DecimalFormat("#.##");
        StringBuilder sb = new StringBuilder();
        sb.append("--FATURAS--\n");
        sb.append("Proprietário: ").append(ci.getNomeP()).append("\nNif do proprietário: ").append(ci.getNifP() + "\n");
        sb.append("Valor: ").append(df.format(fatura.get(ci))).append("€");
        sb.append("\n-----------\n");
        return sb.toString();
    }
    
    public String nifstoString(){
       StringBuilder sb = new StringBuilder();
       for (Map.Entry<Long,CasaInteligente> casa : casas.entrySet()){
           sb.append("->").append(casa.getKey()).append("\n");;
        }
       return sb.toString();
    }
    
    public String ffornecedorestoString(String com){
        StringBuilder sb = new StringBuilder();
        Comercializadores forn = comercializadores.get(com);
        return forn.faturasforn();
    }
    
    public String forntoString(){
       StringBuilder sb = new StringBuilder();
       for (Map.Entry<String,Comercializadores> com : comercializadores.entrySet()){
           sb.append("->").append(com.getKey()).append("\n");;
        }
        return sb.toString();
    }
    
}
