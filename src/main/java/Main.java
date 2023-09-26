import dao.*;
import model.Anime;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        System.out.println("BEM VINDO AO MENU DE AEDS 3");
        System.out.println("===============================================");
        menu();

    }

    public static void menu() throws Exception {
        Scanner sc = new Scanner(System.in);
        int op = 3;
        boolean loop = true;
        do {
            System.out.println("Qual modo você deseja utilizar?");
            System.out.println("(1) - Indexação em tempo real");
            System.out.println("(2) - Sem indexação");
            System.out.println("(3) - Sair");
            op = lerOpcao(sc);
            if (op > 3 || op < 1) {
                System.out.println("Opcao invalida");
            } else {
                loop = false;
            }
        } while (loop);
        if (op != 3) {
            opcoesMenu(op, sc);
        }
        sc.close();
    }

    public static void opcoesMenu(int op, Scanner sc) throws Exception {
        int opMenu = 0;
        String nomeCsv;
        String nomeBin;
        String nomeArquivo = lerNomeArquivo(sc);
        nomeCsv = nomeArquivo + ".csv";
        nomeBin = nomeArquivo + "Bin.bin";
        AnimeDAO animeDAO = new AnimeDAO(nomeBin, nomeCsv);
        Anime anime = new Anime();
        animeDAO.csvToByte();
        String BPlusTreeName = nomeArquivo + "Bplus.bin";
        String listaInvertidaTipo = nomeArquivo + "ListaTipo.bin";
        String listaInvertidaStudio = nomeArquivo + "ListaStudio.bin";
        String dynamicHash = nomeArquivo + "dynamicH.bin";
        if (op == 1) {
            indexarAnimes(nomeArquivo, animeDAO, sc);
            BPlusTreeDAO bPlusTreeDAO = new BPlusTreeDAO(BPlusTreeName);
            ListaInvertidaDAO listaInvertidaDAOType = new ListaInvertidaDAO(listaInvertidaTipo);
            ListaInvertidaDAO listaInvertidaDAOStudio = new ListaInvertidaDAO(listaInvertidaStudio);
            DynamicHashingDAO dynamicHashingDAO = new DynamicHashingDAO(dynamicHash, false);
        } else {

        }
        boolean loop = true;
        int id = 0;
        do {
            showMenu();
            opMenu = lerOpcao(sc);
            if (op == 1) {
                switch (opMenu) {
                    case 1:
                        id = lerOpcao(sc);
                        break;
                    case 2:
                        id = lerOpcao(sc);
                        break;
                    case 3:
                        id = lerOpcao(sc);
                        break;
                    case 4:
                        id = lerOpcao(sc);
                        break;
                    case 5:
                        id = lerOpcao(sc);
                        break;
                    case 6:
                        id = lerOpcao(sc);
                        break;
                    case 7:
                        RecordDAO recordDAO = new RecordDAO(nomeBin);
                        System.out.println("Digite a quantidade de caminhos");
                        int caminhos = lerOpcao(sc);
                        System.out.println("Digite o tamanho do bloco");
                        int blocos = lerOpcao(sc);
                        recordDAO.intercalacaoBalanceada(caminhos,blocos);
                        System.out.println("Arquivo principal reordenado, reindexação necessária");
                        break;
                    case 8:
                        indexarAnimes(nomeArquivo, animeDAO, sc);
                        BPlusTreeDAO bPlusTreeDAO = new BPlusTreeDAO(BPlusTreeName);
                        ListaInvertidaDAO listaInvertidaDAOType = new ListaInvertidaDAO(listaInvertidaTipo);
                        ListaInvertidaDAO listaInvertidaDAOStudio = new ListaInvertidaDAO(listaInvertidaStudio);
                        DynamicHashingDAO dynamicHashingDAO = new DynamicHashingDAO(dynamicHash, false);
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
                        id = lerOpcao(sc);
                        anime = animeDAO.searchAnimeById(id);
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
                        id = lerOpcao(sc);
                        animeDAO.removeAnime(id);
                        break;
                    case 4:
                        anime = lerAnime(sc);

                        break;
                    case 5:
                        System.out.println("Arquivo não indexado");
                        break;
                    case 6:
                        System.out.println("Arquivo não indexado");
                        break;
                    case 7:
                        RecordDAO recordDAO = new RecordDAO(nomeBin);
                        System.out.println("Digite a quantidade de caminhos");
                        int caminhos = lerOpcao(sc);
                        System.out.println("Digite o tamanho do bloco");
                        int blocos = lerOpcao(sc);
                        recordDAO.intercalacaoBalanceada(caminhos,blocos);
                        System.out.println("Arquivo principal reordenado, reindexação necessária");
                        break;
                    case 8:
                        op = 1;
                        indexarAnimes(nomeArquivo, animeDAO, sc);
                        BPlusTreeDAO bPlusTreeDAO = new BPlusTreeDAO(BPlusTreeName);
                        ListaInvertidaDAO listaInvertidaDAOType = new ListaInvertidaDAO(listaInvertidaTipo);
                        ListaInvertidaDAO listaInvertidaDAOStudio = new ListaInvertidaDAO(listaInvertidaStudio);
                        DynamicHashingDAO dynamicHashingDAO = new DynamicHashingDAO(dynamicHash, false);
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
        String dynamicHash = nomeArquivo + "dynamicH.bin";
        System.out.println("Digite a ordem da árvore: ");
        BPlusTreeDAO bPlusTreeDAO = new BPlusTreeDAO(BPlusTreeName, lerOpcao(sc));
        ListaInvertidaDAO listaInvertidaDAOType = new ListaInvertidaDAO(listaInvertidaTipo, true);
        ListaInvertidaDAO listaInvertidaDAOStudio = new ListaInvertidaDAO(listaInvertidaStudio, true);
        DynamicHashingDAO dynamicHashingDAO = new DynamicHashingDAO(dynamicHash, true);
        animeDAO.criarListaInvertidaType(listaInvertidaDAOType);
        animeDAO.criarListaInvertidaStudio(listaInvertidaDAOStudio);
        animeDAO.buildBPlusTreeIndexFile(bPlusTreeDAO);
        animeDAO.buildHashIndexFile(dynamicHashingDAO);
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
        System.out.println("(5) - Buscar animes por tipo");
        System.out.println("(6) - Buscar anime por estúdio");
        System.out.println("(7) - Ordenar Animes");
        System.out.println("(8) - Indexar Animes");
        System.out.println("(0) - Sair");
    }

    public static int lerOpcao(Scanner sc) {

        int saida = -1;
        char op = sc.nextLine().charAt(0);
//        if (sc.hasNext()) {
//            sc.nextLine();
//        }
        saida = Character.getNumericValue(op);
        return saida;

    }
    public static Anime lerAnime(Scanner sc) {
        Anime a = new Anime();



        return a;
    }


}
