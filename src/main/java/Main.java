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
        String dynamicHash = nomeArquivo + "dynamicH.bin";
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
                        switch (indexOption) {
                            case 1:
                                anime = animeDAO.indexSearchInBPlusTree(id, bPlusTreeDAO);
                                break;
                            case 2:
                                anime = animeDAO.indexSearchInHash(id, dynamicHashingDAO);
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
                        switch (indexOption) {
                            case 1:
                                animeDAO.removeAnimeWithBPlusTree(id, bPlusTreeDAO);
                                break;
                            case 2:
                                animeDAO.removeAnimeWithHash(id, dynamicHashingDAO);
                                break;
                            default:
                                System.out.println("Invalido");
                                break;
                        }
                        id = lerOpcao(sc);
                        animeDAO.removeAnime(id);
                        break;
                    case 4:
                        System.out.println("Inserindo Anime");
                        anime = lerAnime(sc);
                        long pos = animeDAO.createAnime(anime);
                        animeDAO.indexInsertInBplusTree(pos, bPlusTreeDAO);
                        animeDAO.indexInsertInHash(pos, dynamicHashingDAO);
                        break;
                    case 5:
                        System.out.println("Atualização sequencial - Arquivo não indexado");
                        System.out.println("Digite o ID do anime a ser atualizado");
                        id = lerOpcao(sc);
                        anime = lerAnime(sc);
                        animeDAO.updateRecord(id, anime);
                        break;
                    case 6:
                        id = lerOpcao(sc);
                        break;
                    case 7:
                        id = lerOpcao(sc);
                        break;
                    case 8:
                        RecordDAO recordDAO = new RecordDAO(nomeBin);
                        System.out.println("Digite a quantidade de caminhos");
                        int caminhos = lerOpcao(sc);
                        System.out.println("Digite o tamanho do bloco");
                        int blocos = lerOpcao(sc);
                        op = 2;
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
        String dynamicHash = nomeArquivo + "dynamicH.bin";
        System.out.println("Digite a ordem da árvore: ");
        BPlusTreeDAO bPlusTreeDAO = new BPlusTreeDAO(BPlusTreeName, lerOpcao(sc));
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
        char op = sc.nextLine().charAt(0);
//        if (sc.hasNext()) {
//            sc.nextLine();
//        }
        saida = Character.getNumericValue(op);
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

    public static Anime lerAnime(Scanner sc) {
        Anime a = new Anime();
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
        a.release_year = a.longToTimestamp(Long.parseLong(sc.nextLine()));
        return a;
    }


}
