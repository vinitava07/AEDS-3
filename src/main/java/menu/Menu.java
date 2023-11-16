package menu;
import dao.AnimeDAO;
import dao.BPlusTreeDAO;
import dao.DynamicHashingDAO;
import dao.ListaInvertidaDAO;
import model.Anime;
import model.BPlusTreePage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Menu {
    private static String[] inputValues;
    private static boolean yes = true;
    private static boolean dataLoaded = false;
    private static boolean dataSorted = false;
    private static boolean dataIndexed = false;
    private static JPanel cards;
    private static CardLayout cardLayout;

    private static SwingWorker<Void, Integer> worker;

    public static void main(String[] args) {
        TaskMonitor.init();

        JFrame frame = new JFrame("Minha GUI de Animes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 720);
        frame.setBackground(new Color(9, 64, 178));

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        frame.add(cards);

        JPanel telaInicial = criarTelaInicial();

        JPanel telaInserir = criarTela("Inserir Anime");
        JPanel telaExcluir = criarTela("Excluir Anime");
        JPanel telaAtualizar = criarTela("Atualizar Anime");
        JPanel telaProcurar = criarTela("Procurar Anime");
        JPanel telaOrdenar = criarTela("Ordenar Animes");

        cards.add(telaInicial, "telaInicial");
        cards.add(telaInserir, "Inserir Anime");
        cards.add(telaExcluir, "Excluir Anime");
        cards.add(telaAtualizar, "Atualizar Anime");
        cards.add(telaProcurar, "Procurar Anime");
        cards.add(telaOrdenar, "Ordenar Animes");

        JPanel botoesPanel = new JPanel();
        frame.add(botoesPanel, BorderLayout.SOUTH);

        JButton botaoInserir = criarBotaoInserir(frame);
        JButton botaoExcluir = criarBotaoExcluir(frame);
        JButton botaoAtualizar = criarBotaoAtualizar(frame);
        JButton botaoProcurar = criarBotaoProcurar(frame);

        JButton botaoComprimir = criarBotaoComprimir(frame);

//        JButton botaoInserir = criarBotaoAcao("Inserir Anime", "Inserir Anime");
//        JButton botaoExcluir = criarBotaoAcao("Excluir Anime", "Excluir Anime");
//        JButton botaoAtualizar = criarBotaoAcao("Atualizar Anime", "Atualizar Anime");
//        JButton botaoProcurar = criarBotaoAcao("Procurar Anime", "Procurar Anime");
        JButton botaoOrdenar = criarBotaoAcao("Ordenar Animes", "Ordenar Animes");
        JButton botaoReindexar = criarBotaoAcao("Reindexar", "Reindexar");

        botoesPanel.add(botaoInserir);
        botoesPanel.add(botaoExcluir);
        botoesPanel.add(botaoAtualizar);
        botoesPanel.add(botaoProcurar);
        botoesPanel.add(botaoOrdenar);
        botoesPanel.add(botaoReindexar);
        botoesPanel.add(botaoComprimir);

        frame.setVisible(true);
    }

    private static JPanel criarTelaInicial() {
        JPanel screen = new JPanel();
        JButton initButton = new JButton("LOAD DATA");
        initButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                TaskMonitor.buildTask("Initializing application");
                Thread task = new Thread() {
                    @Override
                    public void run() {
                        System.out.println("CSV to Byte thread started");
                        AnimeDAO animeDAO = new AnimeDAO("animeBin.bin" , "ListaAnime.csv");
                        animeDAO.csvToByte();
                        this.interrupt();
                        System.out.println("CSV to Byte thread finished");
                    }
                };
                try {
                    task.start();
                    Thread.sleep(1);
                } catch (Exception error) {
                    error.printStackTrace();
                }
                worker = new SwingWorker<Void, Integer>() {
                    ProgressMonitor progressMonitor = new ProgressMonitor(screen,
                            TaskMonitor.getTaskName(),
                            "Working on it...",
                            0, 101);
                    @Override
                    protected Void doInBackground() throws Exception {

                        while (!progressMonitor.isCanceled()) {
                            progressMonitor.setProgress(TaskMonitor.getProgress());
                            progressMonitor.setNote(
                                    String.format("Completed %d%%.\n", TaskMonitor.getProgress()));
                            if(TaskMonitor.getProgress() == 100) {
                                progressMonitor.setNote("Completed 100%.\n");
                                Menu.dataLoaded = true;
                                break;
                            }
                        }
                        task.join();
                        System.out.println("CSV to Byte thread joined");
                        int option = JOptionPane.showConfirmDialog(screen, "Deseja indexar os dados?", "Indexação", JOptionPane.YES_NO_OPTION);
                        if(option == JOptionPane.YES_OPTION){
                            System.out.println("Indexing data");
                            TaskMonitor.buildTask("Indexing");
                            Thread task = new Thread() {
                                @Override
                                public void run() {
                                    System.out.println("Indexing thread started");
                                    Menu.indexarAnimes(new AnimeDAO("animeBin.bin", null));
                                    this.interrupt();
                                    System.out.println("Indexing thread finished");
                                }
                            };
                            try {
                                task.start();
                                Thread.sleep(1);
                            } catch (Exception error) {
                                error.printStackTrace();
                            }
                            boolean change = false;
                            String note = "Please wait... Completed(";
                            int aux = 0;
                            progressMonitor.setNote(note + "0/4)");
//                            ProgressMonitor progressMonitor2 = new ProgressMonitor(screen, (TaskMonitor.getTaskName() + " in Progress"), (note + "0/4)"), 0, 0);
                            while (!progressMonitor.isCanceled()) {
                                progressMonitor.setProgress(TaskMonitor.getProgress());
                                if (change == Menu.yes) {
                                    progressMonitor.setNote(note + String.format("%d/4)", ++aux));
                                    change = !change;
                                }
                                if(aux == 4) {
                                    progressMonitor.setNote(note + "4/4)");
                                    Menu.dataIndexed = true;
                                    break;
                                }
                            }
                            progressMonitor.setProgress(101);
                            task.join();
                            System.out.println("Indexing thread joined");
                            System.out.println("Indexing finished");
                        } else {
                            Menu.dataIndexed = false;
                            progressMonitor.setProgress(101);
                        }
                        return null;
                    }


                    @Override
                    protected void process(java.util.List<Integer> chunks) {
                        // Update the progress bar in the Event Dispatch Thread
                        int progressValue = chunks.get(chunks.size() - 1);
                        progressMonitor.setProgress(progressValue);
                    }
                };

                worker.execute(); // Start the SwingWorker
            }
        });
        screen.add(initButton, BorderLayout.NORTH);
        screen.setBackground(new Color(116, 146, 206));
        return screen;
    }


    private static JPanel criarTela(String titulo) {
        JPanel tela = new JPanel();
        tela.setLayout(new BorderLayout());
        JLabel label = new JLabel(titulo);
        tela.add(label, BorderLayout.CENTER);

        JButton botaoVoltar = new JButton("Voltar para Tela Inicial");
        botaoVoltar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "telaInicial");
            }
        });
        tela.add(botaoVoltar, BorderLayout.SOUTH);
        tela.setBackground(new Color(116, 146, 206));
        return tela;
    }

    private static JButton criarBotaoAcao(String nomeBotao, final String nomeTela) {
        JButton botao = new JButton(nomeBotao);
        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, nomeTela);
            }
        });
        return botao;
    }

    public static void indexarAnimes(AnimeDAO animeDAO) {
        String BPlusTreeName = "ListaAnimeBplus.bin";
        String listaInvertidaTipo = "ListaAnimeListaTipo.bin";
        String listaInvertidaStudio = "ListaAnimeListaStudio.bin";
        String dynamicHash = "ListaAnimeDynamicHash.bin";
        BPlusTreeDAO bPlusTreeDAO = new BPlusTreeDAO(BPlusTreeName, 8);
        ListaInvertidaDAO listaInvertidaDAOType = new ListaInvertidaDAO(listaInvertidaTipo, true);
        ListaInvertidaDAO listaInvertidaDAOStudio = new ListaInvertidaDAO(listaInvertidaStudio, true);
        DynamicHashingDAO dynamicHashingDAO = new DynamicHashingDAO(dynamicHash, true);
        try {
            animeDAO.buildBPlusTreeIndexFile(bPlusTreeDAO);
            Menu.yes = !Menu.yes;
            System.out.println("BPlusTree index built");
            Thread.sleep(1);
            animeDAO.buildHashIndexFile(dynamicHashingDAO);
            Menu.yes = !Menu.yes;
            System.out.println("DynamicHash index built");
            Thread.sleep(1);
            animeDAO.criarListaInvertidaType(listaInvertidaDAOType);
            Menu.yes = !Menu.yes;
            System.out.println("ListaInvertidaType index built");
            Thread.sleep(1);
            animeDAO.criarListaInvertidaStudio(listaInvertidaDAOStudio);
            Menu.yes = !Menu.yes;
            System.out.println("ListaInvertidaStudio index built");
            Thread.sleep(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JButton criarBotaoInserir(JFrame frame) {
        JButton button = new JButton("Inserir Anime");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Name,Type,Episodes,Studio,Tags,Rating,Release_year
                String[] fieldsName = {"Name", "Type", "Episodes", "Studio", "Tags", "Rating", "Release_year"};
                CustomInputDialog dialog = new CustomInputDialog(frame, fieldsName.length, "Inserir Anime", fieldsName);
                dialog.setVisible(true);
            }
        });
        return button;
    }

    private static JButton criarBotaoExcluir(JFrame frame) {
        JButton button = new JButton("Excluir Anime");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CustomInputDialog dialog = new CustomInputDialog(frame, 1, "Excluir Anime", new String[]{"ID"});
                dialog.setVisible(true);
            }
        });
        return button;
    }

    private static JButton criarBotaoAtualizar(JFrame frame) {
        JButton button = new JButton("Atualizar Anime");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] fieldsName = {"ID", "Name", "Type", "Episodes", "Studio", "Tags", "Rating", "Release_year"};
                CustomInputDialog dialog = new CustomInputDialog(frame, fieldsName.length, "Atualizar Anime", fieldsName);
                dialog.setVisible(true);
            }
        });
        return button;
    }

    private static JButton criarBotaoProcurar(JFrame frame) {
        JButton button = new JButton("Procurar Anime");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CustomInputDialog dialog = new CustomInputDialog(frame, 1, "Procurar Anime", new String[]{"ID"});
                dialog.setVisible(true);
            }
        });
        return button;
    }

    // TODO : Implementar botão Ordenar Animes

    // TODO : Implementar botão Reindexar

    private static JButton criarBotaoComprimir(JFrame frame) {
        JButton button = new JButton("Comprimir");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Menu.dataLoaded) {
                    String[] options = {"LZW", "Huffman", "LZ77"};
                    int userChoice = JOptionPane.showOptionDialog(frame,
                            "Choose an option:",
                            "Compression Algorithm",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                    AnimeDAO animeDAO = new AnimeDAO("animeBin.bin", "ListaAnime.csv");
                    if (userChoice == 0) {
                        animeDAO.LZWCompression();
                    } else if (userChoice == 1) {
                        double originalSize = new File(animeDAO.arquivo.csvFile).length();
                        long compressedSize = animeDAO.huffmanCompression();
                        JOptionPane.showMessageDialog(frame, String.format("Compressed size: %.2f MB\nCompression rate: %.0f%%", (((double) compressedSize) / Math.pow(2, 20)) , ((compressedSize / originalSize) * 100)));
                    } else if (userChoice == 2) {
                        // TODO : Implementar LZ77
                    } else {
                        JOptionPane.showMessageDialog(frame, "Nenhuma opção selecionada");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Dados não carregados");
                }
            }
        });
        return button;
    }

    public static void setInputValues(String[] inputValues) {
        Menu.inputValues = inputValues;
        for (int i = 0; i < Menu.inputValues.length; i++) {
            System.out.println(Menu.inputValues[i]);
        }
    }
}

class CustomInputDialog extends JDialog {
    private JTextField[] fields;

    public CustomInputDialog(JFrame parent, int numFields, String title, String[] fieldsName) {
        super(parent, title, true);
        setLayout(new GridLayout(numFields + 2, 2));
        fields = new JTextField[numFields];
        createAndAddFields(fieldsName);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] inputValues = new String[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    inputValues[i] = fields[i].getText();
                }
                Menu.setInputValues(inputValues);
                // Close the dialog when the OK button is clicked
                dispose();
            }
        });
        add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Set input values to null and close the dialog when the Cancel button is clicked
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setText(null);
                }
                dispose();
            }
        });
        add(cancelButton);

        // Set the size of the dialog based on the components
        pack();

        // Center the dialog on the parent frame
        setLocationRelativeTo(parent);
    }

    private void createAndAddFields(String[] fieldsName) {
        for (int i = 0; i < fields.length; i++) {
            add(new JLabel(fieldsName[i] + ":"));
            fields[i] = new JTextField(50);
            add(fields[i]);
        }
    }
}

/*
* public void actionPerformed(ActionEvent e) {

                TaskMonitor.buildTask("CSV to Byte");
                Thread task = new Thread() {
                    @Override
                    public void run() {
                        AnimeDAO animeDAO = new AnimeDAO("animeBin.bin" , "ListaAnime.csv");
                        animeDAO.csvToByte();
                        while (this.isAlive()) ;
                        this.interrupt();
                    }
                };
                try {
                    task.start();
                    Thread.sleep(1);
                } catch (Exception error) {
                    error.printStackTrace();
                }
                worker = new SwingWorker<Void, Integer>() {
                    ProgressMonitor progressMonitor = new ProgressMonitor(screen,
                            TaskMonitor.getTaskName(),
                            "Working on it...",
                            0, 100);
                    @Override
                    protected Void doInBackground() throws Exception {

                        while (!progressMonitor.isCanceled()) {
                            publish(TaskMonitor.getProgress());
                            progressMonitor.setNote(
                                    String.format("Completed %d%%.\n", TaskMonitor.getProgress()));
                        }
                        task.join();
                        progressMonitor.close();
                        return null;
                    }


                    @Override
                    protected void process(java.util.List<Integer> chunks) {
                        // Update the progress bar in the Event Dispatch Thread
                        int progressValue = chunks.get(chunks.size() - 1);
                        progressMonitor.setProgress(progressValue);
                    }
                };

                worker.execute(); // Start the SwingWorker
            }*/