import dao.*;
import model.*;
import util.*;

import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        System.out.print("\033[H\033[2JBEM VINDO AO MENU DE AEDS 3\n");
        System.out.println("===============================================");
        menu();
//        AnimeDAO a = new AnimeDAO("ListaAnimeBin.bin","ListaAnime.csv");
//        a.csvToByte();
//        a.uncipherFile();
//        System.out.println("procura ");
//        a.searchAnimeById(10);
//        RSADAO rsadao = new RSADAO();
//        rsadao.criptografar();


    }

    public static void menu() throws Exception {
        Scanner sc = new Scanner(System.in);
        int op = 3;
        boolean loop = true;
        do {
            System.out.println("Qual modo você deseja utilizar?");
            System.out.println("(1) - Indexação em tempo real");
            System.out.println("(2) - Sem indexação");
            System.out.println("(3) - Compressão e Casamento de padrões");
            System.out.println("(4) - Sair");
            op = lerOpcao(sc);
            if (op > 4 || op < 1) {
                System.out.println("Opcao invalida");
            } else {
                loop = false;
            }
        } while (loop);

        if (op == 1 || op == 2) {
            opcoesMenu(op, sc);
        } else if (op == 3) {
            menuCompPM(sc);
        }
        sc.close();
    }

    public static void menuCompPM(Scanner sc) {
        String nomeCsv;
        String nomeBin;
        int opMenu;
//        String nomeArquivo = lerNomeArquivo(sc);
        String nomeArquivo = "ListaAnime";
        nomeCsv = nomeArquivo + ".csv";


        do {
            showMenuPM();
            opMenu = lerOpcao(sc);
            String pattern;
            switch (opMenu) {
                case 1:
                    System.out.println("Digite o padrão que deseja buscar: ");
                    pattern = sc.nextLine();
                    KMPDAO kmpdao = new KMPDAO(pattern);
                    try (RandomAccessFile raf = new RandomAccessFile("../resources/" + nomeCsv, "rw")) {
                        StringBuilder sb = new StringBuilder();
                        String text;
                        System.out.println("Carregando arquivo csv");
                        while (raf.getFilePointer() < raf.length()) {
                            sb.append(raf.readLine());
                            sb.append('\n');
                        }
                        System.out.println("Leitura finalizada");
                        text = sb.toString();
                        kmpdao.searchPattern(text);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("O número de comparações foi: " + kmpdao.getNComp());
                    System.out.println("Função de falha: ");
                    kmpdao.printFailure();
                    break;
                case 2:
                    System.out.println("Digite o padrão que deseja buscar: ");
                    pattern = sc.nextLine();
                    BoyerMooreDAO boyerMooreDAO = new BoyerMooreDAO(pattern, nomeCsv);
                    boyerMooreDAO.searchPattern();

                    break;
                case 3:
                    HuffmanDAO huffmanDAO = new HuffmanDAO();
                    huffmanDAO.compressFile("../resources/" + nomeCsv);
                    huffmanDAO.deCompressFile();


                    break;
                case 4:
                    LZWDAO lzwdao = new LZWDAO(nomeCsv);
                    lzwdao.createCompressedFile();
                    lzwdao.decompressFile();
                    break;
                case 5:
                    System.out.println("Saindo do programa");
                    break;
                default:
                    System.out.println("Opção inválida");
                    break;
            }

        } while (opMenu != 5);

    }

    public static void showMenuPM() {

        System.out.println("O que deseja fazer?");
        System.out.println("(1) - Buscar padrão por KMP");
        System.out.println("(2) - Buscar padrão por Boyer Moore");
        System.out.println("(3) - Comprimir com Huffman");
        System.out.println("(4) - Comprimir com LZW");
        System.out.println("(5) - Sair");

    }

    public static void opcoesMenu(int op, Scanner sc) throws Exception {
        int opMenu = 0;
        String nomeCsv;
        String nomeBin;
//        String nomeArquivo = lerNomeArquivo(sc);
        String nomeArquivo = "ListaAnime";
        nomeCsv = nomeArquivo + ".csv";
        nomeBin = nomeArquivo + "Bin.bin";
        AnimeDAO animeDAO = new AnimeDAO(nomeBin, nomeCsv);
        Anime anime = new Anime();
        animeDAO.csvToByte();
        String BPlusTreeName = nomeArquivo + "Bplus.bin";
        String listaInvertidaTipo = nomeArquivo + "ListaTipo.bin";
        String listaInvertidaStudio = nomeArquivo + "ListaStudio.bin";
        String dynamicHash = nomeArquivo + "DynamicHash.bin";
        BPlusTreeDAO bPlusTreeDAO = null;
        ListaInvertidaDAO listaInvertidaDAOType = null;
        ListaInvertidaDAO listaInvertidaDAOStudio = null;
        DynamicHashingDAO dynamicHashingDAO = null;
        if (op == 1) {
            indexarAnimes(nomeArquivo, animeDAO, sc);
            bPlusTreeDAO = new BPlusTreeDAO(BPlusTreeName);
            listaInvertidaDAOType = new ListaInvertidaDAO(listaInvertidaTipo);
            listaInvertidaDAOStudio = new ListaInvertidaDAO(listaInvertidaStudio);
            dynamicHashingDAO = new DynamicHashingDAO(dynamicHash, false);
        }
        boolean loop = true;
        int id = 0;
        long pos = -1;
        int indexOption = -1;
        do {
            showMenu();
            opMenu = lerOpcao(sc);
            if (op == 1) {
                switch (opMenu) {
                    case 1:
                        System.out.println("Digite o ID buscado");
                        id = lerOpcao(sc);
                        indexOption = indexMenu(sc);
                        ProgressMonitor searchProgress = new ProgressMonitor(("Searching id: " + id + " "));
                        switch (indexOption) {
                            case 1:
                                searchProgress.start();
                                anime = animeDAO.indexSearchInBPlusTree(id, bPlusTreeDAO);
                                searchProgress.endProcess();
                                searchProgress.join();
                                break;
                            case 2:
                                searchProgress.start();
                                anime = animeDAO.indexSearchInHash(id, dynamicHashingDAO);
                                searchProgress.endProcess();
                                searchProgress.join();
                                break;
                            default:
                                System.out.println("Invalido");
                                anime = null;
                                break;
                        }
                        if (anime != null) {
                            anime.printAttributes();
                        } else {
                            System.out.println("Não encontrado");
                        }
                        break;
                    case 2:
                        animeDAO.printAllAnime();
                        break;
                    case 3:
                        System.out.println("Digite o ID do anime a ser apagado");
                        id = lerOpcao(sc);
                        indexOption = indexMenu(sc);
                        ProgressMonitor deleteProgress = new ProgressMonitor("Deleting");
                        switch (indexOption) {
                            case 1:
                                deleteProgress.start();
                                pos = animeDAO.removeAnimeWithBPlusTree(id, bPlusTreeDAO);
                                bPlusTreeDAO.deleteElement(id);
                                break;
                            case 2:
                                deleteProgress.start();
                                pos = animeDAO.removeAnimeWithHash(id, dynamicHashingDAO);
                                dynamicHashingDAO.removeElement(id);
                                break;
                            default:
                                System.out.println("Invalido");
                                break;
                        }
                        animeDAO.removeListaInvertidaType(id, pos, listaInvertidaDAOType, bPlusTreeDAO);
                        animeDAO.removeListaInvertidaStudio(id, pos, listaInvertidaDAOStudio, bPlusTreeDAO);
                        deleteProgress.endProcess();
                        deleteProgress.join();
                        break;
                    case 4:
                        System.out.println("Inserindo Anime");
                        anime = lerAnime(sc);
                        pos = animeDAO.createAnime(anime);
                        animeDAO.indexInsertInBplusTree(pos, bPlusTreeDAO);
                        animeDAO.indexInsertInHash(pos, dynamicHashingDAO);
//                        listaInvertidaDAOType.insertType("TV", 500);

                        listaInvertidaDAOType.insertType(anime.type.trim(), pos);
                        listaInvertidaDAOStudio.insertStudio(anime.studio, pos);
                        System.out.println("Anime inserido");
                        break;
                    case 5:
                        System.out.println("Digite o ID do anime a ser Atualizado");
                        id = lerOpcao(sc);
                        anime = lerAnime(sc);
                        indexOption = indexMenu(sc);
//                        animeDAO.updateRecord(id, anime);
                        long oldPointer = bPlusTreeDAO.search(id);
                        System.out.println(oldPointer);
                        PageElement e = null;
                        switch (indexOption) {
                            case 1:
                                pos = animeDAO.updateWithBPlus(id, anime, bPlusTreeDAO);
                                e = new PageElement(id, pos - 8);
                                dynamicHashingDAO.updateElement(e);
                                break;
                            case 2:
                                pos = animeDAO.updateWithDynamicHash(id, anime, dynamicHashingDAO);
                                e = new PageElement(id, pos);
                                bPlusTreeDAO.updateElement(e);
                                break;
                            default:
                                System.out.println("Invalido");
                                break;
                        }
                        listaInvertidaDAOType.updateIndex(anime.type, oldPointer - 8, pos - 8);
                        listaInvertidaDAOStudio.updateIndex(anime.studio, oldPointer - 8, pos - 8);
                        break;
                    case 6:
                        System.out.println("Buscar animes por type");
                        listaInvertidaDAOType.printIndex(animeDAO);
                        break;
                    case 7:
                        System.out.println("Buscar anime por studio");
                        listaInvertidaDAOStudio.printIndex(animeDAO);
                        break;
                    case 8:
                        RecordDAO recordDAO = new RecordDAO(nomeBin);
                        System.out.println("Digite a quantidade de caminhos");
                        int caminhos = lerOpcao(sc);
                        System.out.println("Digite o tamanho do bloco");
                        int blocos = lerOpcao(sc);
                        op = 2;
                        recordDAO.intercalacaoBalanceada(caminhos, blocos);
                        break;
                    case 9:
                        indexarAnimes(nomeArquivo, animeDAO, sc);
                        bPlusTreeDAO = new BPlusTreeDAO(BPlusTreeName);
                        listaInvertidaDAOType = new ListaInvertidaDAO(listaInvertidaTipo);
                        listaInvertidaDAOStudio = new ListaInvertidaDAO(listaInvertidaStudio);
                        dynamicHashingDAO = new DynamicHashingDAO(dynamicHash, false);
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opcao invalida");
                        break;
                }
            } else {
                switch (opMenu) {
                    case 1:
                        System.out.println("Busca sequencial - Arquivo não indexado");
                        System.out.println("Digite o ID a ser buscado");
                        id = lerOpcao(sc);
//                        System.out.println(id);
                        ProgressMonitor searchProgress = new ProgressMonitor(("Search id: " + id));
                        searchProgress.start();
                        anime = animeDAO.searchAnimeById(id);
                        searchProgress.endProcess();
                        searchProgress.join();
                        if (anime != null) {
                            anime.printAttributes();
                        } else {
                            System.out.println("Não encontrado");
                        }
                        break;
                    case 2:
                        animeDAO.printAllAnime();
                        break;
                    case 3:
                        System.out.println("Remoção sequencial - Arquivo não indexado");
                        id = lerOpcao(sc);
                        animeDAO.removeAnime(id);
                        break;
                    case 4:
                        System.out.println("Inserção sequencial - Arquivo não indexado");
                        anime = lerAnime(sc);
                        animeDAO.createAnime(anime);
                        break;
                    case 5:
                        System.out.println("Atualização sequencial - Arquivo não indexado");
                        System.out.println("Digite o ID do anime a ser atualizado");
                        id = lerOpcao(sc);
                        anime = lerAnime(sc);
                        animeDAO.updateRecord(id, anime);
                        break;
                    case 6:
                        System.out.println("Arquivo não indexado");
                        break;
                    case 7:
                        System.out.println("Arquivo não indexado");
                        break;
                    case 8:
                        RecordDAO recordDAO = new RecordDAO(nomeBin);
                        System.out.println("Digite a quantidade de caminhos");
                        int caminhos = lerOpcao(sc);
                        System.out.println("Digite o tamanho do bloco");
                        int blocos = lerOpcao(sc);
                        recordDAO.intercalacaoBalanceada(caminhos, blocos);
                        System.out.println("Arquivo principal reordenado, reindexação necessária");
                        op = 2;
                        break;
                    case 9:
                        op = 1;
                        indexarAnimes(nomeArquivo, animeDAO, sc);
                        bPlusTreeDAO = new BPlusTreeDAO(BPlusTreeName);
                        listaInvertidaDAOType = new ListaInvertidaDAO(listaInvertidaTipo);
                        listaInvertidaDAOStudio = new ListaInvertidaDAO(listaInvertidaStudio);
                        dynamicHashingDAO = new DynamicHashingDAO(dynamicHash, false);
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opcao invalida");
                        break;
                }
            }

        } while (opMenu != 0);
        System.out.println("Programa encerrado");
    }

    public static void indexarAnimes(String nomeArquivo, AnimeDAO animeDAO, Scanner sc) {
        String BPlusTreeName = nomeArquivo + "Bplus.bin";
        String listaInvertidaTipo = nomeArquivo + "ListaTipo.bin";
        String listaInvertidaStudio = nomeArquivo + "ListaStudio.bin";
        String dynamicHash = nomeArquivo + "DynamicHash.bin";
        System.out.println("Digite a ordem da árvore: ");
        BPlusTreeDAO bPlusTreeDAO = new BPlusTreeDAO(BPlusTreeName, lerOpcao(sc));
        System.out.println("Criando listas invertidas");
        ListaInvertidaDAO listaInvertidaDAOType = new ListaInvertidaDAO(listaInvertidaTipo, true);
        ListaInvertidaDAO listaInvertidaDAOStudio = new ListaInvertidaDAO(listaInvertidaStudio, true);
        DynamicHashingDAO dynamicHashingDAO = new DynamicHashingDAO(dynamicHash, true);
        animeDAO.buildBPlusTreeIndexFile(bPlusTreeDAO);
        animeDAO.buildHashIndexFile(dynamicHashingDAO);
        animeDAO.criarListaInvertidaType(listaInvertidaDAOType);
        animeDAO.criarListaInvertidaStudio(listaInvertidaDAOStudio);
    }

    public static String lerNomeArquivo(Scanner sc) {
        System.out.println("Digite o nome do csv");
        String fileName = sc.nextLine();
//        if (sc.hasNext()) {
//            sc.nextLine();
//        }
        return fileName;

    }

    public static void showMenu() {
        System.out.println("O que deseja fazer?");
        System.out.println("(1) - Buscar Anime");
        System.out.println("(2) - Listar Animes");
        System.out.println("(3) - Apagar Anime");
        System.out.println("(4) - Inserir Anime");
        System.out.println("(5) - Atualizar Anime");
        System.out.println("(6) - Buscar animes por tipo");
        System.out.println("(7) - Buscar anime por estúdio");
        System.out.println("(8) - Ordenar Animes");
        System.out.println("(9) - Indexar Animes");
        System.out.println("(0) - Sair");
    }

    public static int lerOpcao(Scanner sc) {

        int saida = -1;
        String op = sc.nextLine();
//        if (sc.hasNext()) {
//            sc.nextLine();
//        }
        saida = Integer.parseInt(op);
        return saida;

    }

    public static int indexMenu(Scanner sc) {
        int option = 0;
        do {
            System.out.println("=====================");
            System.out.println("Digite em qual tipo de index você deseja utilizar");
            System.out.println("(1) - Arvore B+");
            System.out.println("(2) - Hash Extensível");
            System.out.println("======================");
            option = lerOpcao(sc);
        } while (option < 1 && option > 2);

        return option;
    }

    public static Anime lerAnime(Scanner sc) throws ParseException {
        Anime a = new Anime();
        Timestamp t;
        System.out.println("Digite o nome do Anime");
        a.name = sc.nextLine();
        System.out.println("Digite o Type do Anime");
        a.type = sc.nextLine();
        System.out.println("Digite a quantidade de episodios do Anime");
        a.episodes = Integer.parseInt((sc.nextLine()));
        System.out.println("Digite o studio do anime");
        a.studio = sc.nextLine();
        System.out.println("Digite as tags do Anime");
        a.tags = sc.nextLine();
        System.out.println("Digite o rating do anime");
        a.rating = Float.parseFloat(sc.nextLine());
        System.out.println("Digite o ano do anime");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        Date parsedDate = dateFormat.parse(sc.nextLine().replace(".0", ""));
        t = new java.sql.Timestamp(parsedDate.getTime());
        a.release_year = t;
        return a;
    }


}
