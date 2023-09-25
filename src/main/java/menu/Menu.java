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

    public static void main(String[] args) {
        ProgressBar.init("" , 0);

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
        JPanel tela = new JPanel();
        JButton initButton = new JButton("LOAD DATA");
        initButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {
                    AnimeDAO animeDAO = new AnimeDAO("animeBin.bin");
                    tela.add(ProgressBar.getProgressBar() , BorderLayout.CENTER);
                    tela.revalidate();

                    RandomAccessFile raf = new RandomAccessFile(animeDAO.arquivo.mainFile , "r");
                    long length = raf.length();
                    raf.close();

                    BPlusTreeDAO bPlusTreeDAO = new BPlusTreeDAO("animeBin.bin" , 8);
                    DynamicHashingDAO dynamicHashingDAO = new DynamicHashingDAO("animeBin.bin" , true);
                    ProgressBar.setHundredPerCent(length - 4);
                    ProgressBar.setProcessName("B+Tree index");
                    tela.revalidate();
                    animeDAO.buildIndexFile(bPlusTreeDAO);
                    ProgressBar.setHundredPerCent(length - 4);
                    ProgressBar.setProcessName("Hash index");
                    tela.revalidate();
                    animeDAO.buildIndexFile(dynamicHashingDAO);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        tela.add(initButton, BorderLayout.SOUTH);
        tela.setBackground(new Color(116, 146, 206));
        return tela;
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
