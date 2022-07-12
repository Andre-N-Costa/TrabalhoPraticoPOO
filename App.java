import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.net.URI;


public class App
{
    public static Faturador f = new Faturador();
    private Menu menu;
    
    public App(){
        
        
        try{
            f = Faturador.le("a.obj");
        }
        catch(FileNotFoundException e) {System.out.println("não conseguiu ler " + e.getMessage());f = criaStandard();}
        catch(IOException e){System.out.println("não conseguiu ler " + e.getMessage());f = criaStandard();}
        catch(ClassNotFoundException e){System.out.println("não conseguiu ler " + e.getMessage());f = criaStandard();}
        
       
        menu = new Menu(new String[]{"Criar CasaInteligente", 
                                     "Editar CasasInteligentes",
                                     "Mostrar CasasInteligentes",
                                     "Criar Fornecedor de Energia",
                                     "Carregar logs",
                                     "Faturas",
                                     "Mostrar os 5 maiores consumidores de energia"});
                                     
        menu.setHandler(1,()->criarCI());
        menu.setHandler(2,()->editarCI());
        menu.setHandler(3,()->mostrarCI());
        menu.setHandler(4,()->criarFE());
        menu.setHandler(5,()->parse());
        menu.setHandler(6,()->faturas());
        menu.setHandler(7,()->top5());
    }
    
    public Scanner gvInput(){
        Scanner is = new Scanner(System.in);
        return is;
    }
    
    public void criarFE(){
        System.out.println("Nome do novo fornecedor de energia: ");
        String com = gvInput().nextLine();
        if (f.getCom(com) != null) System.out.println("Fornecedor já existe.\nCriação do fornecedor cancelada!");
        else{Comercializadores c = new Comercializadores(com);
             f.addCom(com,c);}
    }
    
    public void faturas(){
        Menu menuF = new Menu(new String[]{"Calcular Faturas", 
                                           "Ver últimas faturas de todas as casas",
                                           "Ver últimas fatura de uma casa",
                                           "Ver casa que mais faturou na última faturação",
                                           "Ver o fornecedor com maior faturação",
                                           "Ver as faturas emitidas por certo fornecedor"});
        
        menuF.setHandler(1,()->cfatura());
        menuF.setHandler(2,()->showfs());
        menuF.setHandler(3,()->showf());
        menuF.setHandler(4,()->showcmf());
        menuF.setHandler(5,()->showfmf());
        menuF.setHandler(6,()->showfef());
        menuF.run();
    }
    
    public void top5(){
        System.out.println(f.simulacaotop5());
    }
    
    public void showfef(){
        System.out.println("Fornecedores disponíveis:\n" + f.forntoString() + "Fornecedor para emitir a fatura: ");
        String energia = gvInput().nextLine();
        if (f.getCom(energia) == null) System.out.println("Fornecedor não existe.\nEmissão de fatura cancelada!");
        System.out.println(f.ffornecedorestoString(energia));
    }
    
    public void showfmf(){
        System.out.println(f.maisFfornecedor());
    }
    
    public void showcmf(){
        System.out.println(f.maisFcasa());
    }
    
    public void cfatura(){
        System.out.println("Data do inicio do cálculo da fatura (AAAA-MM-DD): ");
        String dataI = gvInput().nextLine();
        System.out.println("Data do final do cálculo da fatura (AAAA-MM-DD): ");
        String dataF = gvInput().nextLine();
        f.calcularfatura(dataI,dataF);
        System.out.println(f.faturaString());
    }
    
    public void showfs(){
        if (f.checkfatura()) System.out.println("Ainda não foi feita nenhuma faturação. Operação cancelada!");
        else System.out.println(f.faturaString());
    }
    
    public void showf(){
        if (f.checkfatura()) System.out.println("Ainda não foi feita nenhuma faturação. Operação cancelada!");
        else {System.out.println(f.nifstoString());
              System.out.println("NIF accociado à casa da qual pretende ver a última fatura: ");
              int nifP = gvInput().nextInt();
              if (f.getCasa(nifP) == null) System.out.println("Casa com " + nifP + " associado não existe. Seleção cancelada!");
              else{CasaInteligente ci = f.getCasa(nifP);
                   System.out.println(f.singlefaturaString(ci));}
             }
        }
    
    public void criarCI(){
        System.out.println("Nome do proprietário: ");
        String nomeP = gvInput().nextLine();
        System.out.println("NIF do proprietário: ");
        long nifP = gvInput().nextInt();
        System.out.println("Fornecedores disponíveis:\n" + f.forntoString() + "Nome do Fornecedor de energia:");
        String energia = gvInput().nextLine();
        Comercializadores c = f.getCom(energia);
        if (f.getCom(energia) == null) System.out.println("Fornecedor não existe.\nCriação da Casa Inteligente cancelada!");
        else{CasaInteligente ci = new CasaInteligente(nifP,nomeP,f.getCom(energia));
            this.f.getCasas().put(nifP,ci);
            f.addCasa(ci);}
    }
    
    public void editarCI(){
        System.out.println("Casas disponíveis:\n");
        System.out.println(f.nifstoString());
        System.out.println("Nif do proprietário da casa a editar:");
        long nifP = gvInput().nextInt();
        if (f.getCasa(nifP) == null) System.out.println("Casa com " + nifP + " associado não existe. Seleção cancelada!");
        else{CasaInteligente ci = f.getCasas().get(nifP);
        
             Menu menuECI = new Menu(new String[]{"Adicionar SmartDevice",
                                                  "Editar SmartDevice",
                                                  "Ligar/Desligar os SmartDevices",
                                                  "Editar Informações"});
            menuECI.setHandler(1,()->addSD(ci));
            menuECI.setHandler(2,()->editSD(ci));
            menuECI.setHandler(3,()->editOFSD(ci));
            menuECI.setHandler(4,()->editInf(ci));
            menuECI.run();}
    }
    
    public void editInf(CasaInteligente ci){
        Menu menuEInf = new Menu(new String[]{"Mudar de Fornecedor de energia",
                                              "Mudar nome do proprietário",
                                              "Mudar NIF do proprietário"});
        menuEInf.setHandler(1,()->mudarFornecedor(ci));
        menuEInf.setHandler(2,()->mudarNomeP(ci));
        menuEInf.setHandler(3,()->mudarNifP(ci));
        menuEInf.run();
    }
    
    public void mudarFornecedor(CasaInteligente ci){
        System.out.println("Fornecedores disponíveis:\n" + f.forntoString() + "Novo fornecedor:");
        String energia = gvInput().nextLine();
        if (f.getCom(energia) == null) System.out.println("Fornecedor não existe.\nMudança de fornecedor cancelada!");
        else f.addMudanca(energia,ci);
    }
    
    public void mudarNomeP(CasaInteligente ci){
        System.out.println("Novo nome do proprietário:");
        String nomeP = gvInput().nextLine();
        ci.setNomeP(nomeP);
    }
    
    public void mudarNifP(CasaInteligente ci){
        System.out.println("Novo NIF do proprietário:");
        long nifP = gvInput().nextLong();
        ci.setNifP(nifP);
    }
    
    public void editOFSD(CasaInteligente ci){
        Menu menuOF = new Menu(new String[] {"Ligar/Desligar todos os SmartDevices da casa",
                                             "Ligar/Desligar todos os SmartDevices de uma divisão"});
        menuOF.setHandler(1,()->editallSD(ci));
        menuOF.setHandler(2,()->editallLocationSD(ci));
        menuOF.run();
    }
    
    public void editallLocationSD(CasaInteligente ci){
        System.out.println(ci.toString() + "\nDivisão para Ligar/Desligar os SmartDevices:");
        String sala = gvInput().nextLine();
        System.out.println("1 - Ligar todos os SmartDevices\n2 - Desligar todos os SmartDevices\n3 - Cancelar");
        int onoff = gvInput().nextInt();
        if (onoff == 1) ci.addAllXLocation(sala,true,f);
        else if (onoff == 2) ci.addAllXLocation(sala,false,f);
    }
    
    public void editallSD(CasaInteligente ci){
        System.out.println("1 - Ligar todos os SmartDevices\n2 - Desligar todos os SmartDevices\n3 - Cancelar");
        int onoff = gvInput().nextInt();
        if(onoff == 1) ci.addAllX(true,f);
        else if (onoff == 2){ci.addAllX(false,f);}
        else System.out.println("Opção inválida!");
    }
    
    public void editSD(CasaInteligente ci){
        System.out.println("SmartDevicesDisponiveis:");
        System.out.println(ci.toString());
        System.out.println("Id do SmartDevice a editar:");
        int id = gvInput().nextInt();
        if (ci.hasDevice(id) == false) System.out.println("SmartDevice ||" + id + "|| não existe.\nSeleção cancelada!");
        else{Menu menuESD = ci.newmenuSD(id,f);
             menuESD.run();}
        
    }
    
    public void mostrarCI(){
        for(Map.Entry<Long,CasaInteligente> entry : f.getCasas().entrySet()){
            System.out.println("-------------------\n");
            System.out.println(entry.getValue().toString() + "\n");
        }
    }
    
    public void addSD(CasaInteligente ci){
        Menu menuASD = new Menu(new String[]{"Adicionar SmartBulb",
                                             "Adicionar SmartSpeaker",
                                             "Adicionar SmartCamera" });
        menuASD.setHandler(1,()->addSB(ci));
        menuASD.setHandler(2,()->addSS(ci));
        menuASD.setHandler(3,()->addSC(ci));
        menuASD.run();
    }
    
    public void addSB(CasaInteligente ci){
        System.out.println("Dimensão da SmartBulb:");
        double dimensao = gvInput().nextDouble();
        System.out.println("Valor Base de consumo de energia:");
        double valorB = gvInput().nextDouble();
        System.out.println("Divisão da casa onde adicionar a SmartBulb:");
        String sala = gvInput().nextLine();
        SmartBulb sb = new SmartBulb(dimensao,valorB);
        ci.addDevice(sb);
        if (ci.hasLocation(sala) == false) ci.addLocation(sala);
        ci.addToLocation(sala,sb.getId());
    }
    
    public void addSS(CasaInteligente ci){
        System.out.println("Marca do SmartSpeaker:");
        String marca = gvInput().nextLine();
        System.out.println("Radio do SmartSpeaker:");
        String radio = gvInput().nextLine();
        System.out.println("Valor Base de consumo de energia:");
        double valorB = gvInput().nextDouble();
        System.out.println("Divisão da casa onde adicionar o SmartSpeaker:");
        String sala = gvInput().nextLine();
        SmartSpeaker ss = new SmartSpeaker(marca,radio,valorB);
        ci.addDevice(ss);
        if (ci.hasLocation(sala) == false) ci.addLocation(sala);
        ci.addToLocation(sala,ss.getId());
    }
    
    public void addSC(CasaInteligente ci){
        System.out.println("Resolução da SmartCamera('(ComprimentoxLargura)'):");
        String resolucao = gvInput().nextLine();
        System.out.println("Tamanho dos ficheiros (Megabytes):");
        double tamanho = gvInput().nextDouble();
        System.out.println("Valor Base de consumo de energia:");
        double valorB = gvInput().nextDouble();
        System.out.println("Divisão da casa onde adicionar a SmartCamera:");
        String sala = gvInput().nextLine();
        SmartCamera sc = new SmartCamera(resolucao,tamanho,valorB);
        ci.addDevice(sc);
        if (ci.hasLocation(sala) == false) ci.addLocation(sala);
        ci.addToLocation(sala,sc.getId());
    }
    
    /*
     * 
     * Ler de ficheiro de texto
     * 
     */
     public void parse(){
        List<String> linhas = lerFicheiro("logs.txt.txt");
        String[] linhaPartida;
        String divisao = null;
        CasaInteligente casaMaisRecente = null;
        for (String linha : linhas) {
            linhaPartida = linha.split(":", 2);
            switch(linhaPartida[0]){
                case "Casa":
                    casaMaisRecente = parseCasa(linhaPartida[1]);
                    f.addCasa(casaMaisRecente);
                    break;
                case "Divisao":
                    if (casaMaisRecente == null) System.out.println("Linha inválida.");
                    divisao = linhaPartida[1];
                    casaMaisRecente.addLocation(divisao);
                    break;
                case "SmartBulb":
                    if (divisao == null) System.out.println("Linha inválida.");
                    SmartBulb sb = parseSmartBulb(linhaPartida[1]);
                    casaMaisRecente.addDevice(sb);
                    casaMaisRecente.addToLocation(divisao, sb.getId());
                    break;
                case "SmartSpeaker":
                    if (divisao == null) System.out.println("Linha inválida.");
                    SmartSpeaker ss = parseSmartSpeaker(linhaPartida[1]);
                    casaMaisRecente.addDevice(ss);
                    casaMaisRecente.addToLocation(divisao, ss.getId());
                    break;
                case "SmartCamera":
                    if (divisao == null) System.out.println("Linha inválida.");
                    SmartCamera sc = parseSmartCamera(linhaPartida[1]);
                    casaMaisRecente.addDevice(sc);
                    casaMaisRecente.addToLocation(divisao, sc.getId());
                    break;
                case "Fornecedor":
                    Comercializadores com = parseFornecedor(linhaPartida[1]);
                    f.addCom(com.getCom1(),com);
                    break;
                
                default:
                    System.out.println("Linha inválida.");
                    break;
            }
        }
        System.out.println("done!");
    }
    
    public List<String> lerFicheiro(String nomeFich) {
        List<String> lines = new ArrayList<>();
        try {lines = Files.readAllLines(Paths.get(nomeFich), StandardCharsets.UTF_8);}
        catch(IOException exc) { lines = new ArrayList<>();}
        return lines;
    }

    public CasaInteligente parseCasa(String input){
        String[] campos = input.split(",");
        String nome = campos[0];
        int nif = Integer.parseInt(campos[1]);
        String com = campos[2];
        return new CasaInteligente(nif,nome,f.getCom(com));
    }
    
    public SmartBulb parseSmartBulb(String input){
        String[] campos = input.split(",");
        int tone; 
        if(campos[0] == "Warm") tone = 2;
        else if(campos[0] == "Cold") tone = 0;
        else tone = 1;
        double dim = Double.parseDouble(campos[1]);
        double valorB = Double.parseDouble(campos[2]);
        return new SmartBulb(tone,dim,valorB);
    }
    
    public SmartSpeaker parseSmartSpeaker(String input){
        String[] campos = input.split(",");
        int volume = Integer.parseInt(campos[0]);
        String radio = campos[1];
        String marca = campos[2];
        double valorB = Double.parseDouble(campos[3]);
        return new SmartSpeaker(volume,radio,marca,valorB);
    }
    
    public SmartCamera parseSmartCamera(String input){
        String[] campos = input.split(",");
        String resolucao = campos[0];
        double tamanho = Double.parseDouble(campos[1]);
        double valorB = Double.parseDouble(campos[2]);
        return new SmartCamera(resolucao,tamanho,valorB);
    }
    
    public Comercializadores parseFornecedor(String input){
        String nomeC = input;
        return new Comercializadores(nomeC);
    }

    /*
     * 
     * Caso o programa não consiga ler o ficheiro de leitura usa este método para gerar a base do programa
     * 
     */
    
    public Faturador criaStandard(){
        Comercializadores c1 = new Comercializadores("EDP");
        Comercializadores c2 = new Comercializadores("Endesa");
        Comercializadores c3 = new Comercializadores("GALP");
        Comercializadores c4 = new Comercializadores("Iberdrola");
        Comercializadores c5 = new Comercializadores("MEO Energia");
        Comercializadores c6 = new Comercializadores("EDA");
        f.addCom(c1.getCom1(),c1);
        f.addCom(c2.getCom1(),c2);
        f.addCom(c3.getCom1(),c3);
        f.addCom(c4.getCom1(),c4);
        f.addCom(c5.getCom1(),c5);
        f.addCom(c6.getCom1(),c6);
        return f;
    }
    
    private void executar(){
        this.menu.run();
    }
    
    public static void main(){
        App a = new App();
        
        a.executar();
        
        System.out.println("Fim do programa");
        try{
            f.grava("a.obj");
        }
        catch(IOException e){System.out.println("não consegui gravar " + e.getMessage());}
    }
}
