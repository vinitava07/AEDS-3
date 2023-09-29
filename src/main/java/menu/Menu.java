package menu;
import dao.AnimeDAO;
import dao.BPlusTreeDAO;
import dao.DynamicHashingDAO;
import model.Anime;
import model.BPlusTreePage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.RandomAccessFile;

public class Menu {
    private static JPanel cards;
    private static CardLayout cardLayout;

    private static SwingWorker<Void, Integer> worker;

    public static void main(String[] args) {
        Task.init();

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
        JPanel telaReindexar = criarTela("Reindexar");

        cards.add(telaInicial, "telaInicial");
        cards.add(telaInserir, "Inserir Anime");
        cards.add(telaExcluir, "Excluir Anime");
        cards.add(telaAtualizar, "Atualizar Anime");
        cards.add(telaProcurar, "Procurar Anime");
        cards.add(telaOrdenar, "Ordenar Animes");
        cards.add(telaReindexar, "Reindexar");

        JPanel botoesPanel = new JPanel();
        frame.add(botoesPanel, BorderLayout.SOUTH);

        JButton botaoInserir = criarBotaoAcao("Inserir Anime", "Inserir Anime");
        JButton botaoExcluir = criarBotaoAcao("Excluir Anime", "Excluir Anime");
        JButton botaoAtualizar = criarBotaoAcao("Atualizar Anime", "Atualizar Anime");
        JButton botaoProcurar = criarBotaoAcao("Procurar Anime", "Procurar Anime");
        JButton botaoOrdenar = criarBotaoAcao("Ordenar Animes", "Ordenar Animes");
        JButton botaoReindexar = criarBotaoAcao("Reindexar", "Reindexar");

        botoesPanel.add(botaoInserir);
        botoesPanel.add(botaoExcluir);
        botoesPanel.add(botaoAtualizar);
        botoesPanel.add(botaoProcurar);
        botoesPanel.add(botaoOrdenar);
        botoesPanel.add(botaoReindexar);

        frame.setVisible(true);
    }

    private static JPanel criarTelaInicial() {
        JPanel screen = new JPanel();
        JButton initButton = new JButton("LOAD DATA");
        initButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Create a SwingWorker to simulate a task

                Task.createTask("CSV to Bye" , 10000);
                AnimeDAO animeDAO = new AnimeDAO("animeBin.bin" , "ListaAnime.csv");
                worker = new SwingWorker<Void, Integer>() {
                    ProgressMonitor progressMonitor = new ProgressMonitor(screen,
                            Task.getTaskName(),
                            "Working on it...",
                            0, Task.getTaskLength());
                    @Override
                    protected Void doInBackground() throws Exception {

//                        while (!progressMonitor.isCanceled()) {
//                            publish(Task.progress);
//                            Task.progress++;
//                            Thread.sleep(1);
//                        }
                        animeDAO.csvToByte();

                        progressMonitor.close();
                        Task.endTask();
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
}
