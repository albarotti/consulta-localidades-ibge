package utfpr.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;
import utfpr.http.ConsultaLocalidades;
import utfpr.model.Municipio;
import utfpr.model.UF;
import java.text.Normalizer;
import java.util.Locale;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class TelaLocalidades extends JFrame {
    private final ConsultaLocalidades consulta = new ConsultaLocalidades();
    private final JComboBox<UF> estados = new JComboBox<>();
    private final DefaultListModel<Municipio> modelo = new DefaultListModel<>();
    private final JList<Municipio> municipios = new JList<>(modelo);
    private final JButton carregar = new JButton("Carregar");
    private final JLabel status = new JLabel(" ");
    private final JTextField campoBusca = new JTextField(20);
    private List<Municipio> municipiosDaUF = List.of();
    private boolean carregando;

    public TelaLocalidades() {
        super("Consulta de localidades do IBGE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("UF"));
        estados.setEditable(true);
        estados.setToolTipText("Digite a sigla ou o início do nome e pressione Enter");
        topo.add(estados);
        topo.add(carregar);
        JPanel linhaBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel rotuloBusca = new JLabel("Buscar município:");
        rotuloBusca.setLabelFor(campoBusca);
        linhaBusca.add(rotuloBusca);
        linhaBusca.add(campoBusca);
        JButton botaoLimpar = new JButton("Limpar busca");
            linhaBusca.add(botaoLimpar);

            botaoLimpar.addActionListener(e -> {
                if (carregando) return;

                    estados.setSelectedIndex(-1);
                    municipiosDaUF = List.of();
                    campoBusca.setText("");
                    modelo.clear();

                    campoBusca.setEnabled(false);
                    status.setText("Selecione uma UF para consultar seus municípios.");
                    estados.requestFocusInWindow();
});
        campoBusca.setEnabled(false);

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.add(topo, BorderLayout.NORTH);
        JTextField campoUF = (JTextField) estados.getEditor().getEditorComponent();

DefaultListModel<UF> modeloSugestoes = new DefaultListModel<>();
JList<UF> listaSugestoes = new JList<>(modeloSugestoes);
listaSugestoes.setVisibleRowCount(3);
listaSugestoes.setFocusable(false);

JScrollPane painelSugestoes = new JScrollPane(listaSugestoes);
painelSugestoes.setVisible(false);
cabecalho.add(painelSugestoes, BorderLayout.CENTER);

campoUF.getDocument().addDocumentListener(new DocumentListener() {
    private void atualizarSugestoes() {
        modeloSugestoes.clear();

        String texto = campoUF.getText().strip();
        String termo = normalizar(texto);
        Object selecionado = estados.getSelectedItem();

        boolean jaEscolheu = selecionado instanceof UF
                && texto.equals(selecionado.toString());

        if (!termo.isEmpty() && !jaEscolheu && !carregando) {
            for (int i = 0; i < estados.getItemCount(); i++) {
                UF uf = estados.getItemAt(i);
                String nome = normalizar(uf.getNome());
                String sigla = normalizar(uf.getSigla());

                if (nome.startsWith(termo) || sigla.startsWith(termo)) {
                    modeloSugestoes.addElement(uf);
                }
            }
        }

        painelSugestoes.setVisible(!modeloSugestoes.isEmpty());
        cabecalho.revalidate();
        cabecalho.repaint();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        atualizarSugestoes();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        atualizarSugestoes();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        atualizarSugestoes();
    }
});

listaSugestoes.addMouseListener(new java.awt.event.MouseAdapter() {
    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        int indice = listaSugestoes.locationToIndex(e.getPoint());
        if (indice < 0) return;

        if (!listaSugestoes.getCellBounds(indice, indice).contains(e.getPoint())) {
            return;
        }

        UF escolhida = modeloSugestoes.getElementAt(indice);
        painelSugestoes.setVisible(false);
        estados.setSelectedItem(escolhida);
        estados.getEditor().setItem(escolhida);
        cabecalho.revalidate();
        cabecalho.repaint();
    }
});
        cabecalho.add(linhaBusca, BorderLayout.SOUTH);
        painel.add(cabecalho, BorderLayout.NORTH);
        JScrollPane lista = new JScrollPane(municipios);
        lista.setBorder(BorderFactory.createTitledBorder("Municípios"));
        painel.add(lista, BorderLayout.CENTER);
        JPanel rodape = new JPanel(new BorderLayout(8, 0));
rodape.add(status, BorderLayout.CENTER);

JButton botaoCuriosidade = new JButton("Curiosidades do Estado :) ");
rodape.add(botaoCuriosidade, BorderLayout.EAST);

painel.add(rodape, BorderLayout.SOUTH);

botaoCuriosidade.addActionListener(e -> {
    UF uf = (UF) estados.getSelectedItem();

    if (uf == null) {
        JOptionPane.showMessageDialog(this, "Selecione uma UF primeiro.");
        return;
    }

    if (carregando || municipiosDaUF.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Aguarde o carregamento dos municípios ou clique em Carregar.");
        return;
    }

    String nomeLongo = municipiosDaUF.get(0).getNome();
    String nomeCurto = nomeLongo;

    for (Municipio municipio : municipiosDaUF) {
        String nome = municipio.getNome();

        if (nome.length() > nomeLongo.length()) {
            nomeLongo = nome;
        }

        if (nome.length() < nomeCurto.length()) {
            nomeCurto = nome;
        }
    }

    String[] curiosidades = {
        "Um dos nomes mais longos é " + nomeLongo
                + ", com " + nomeLongo.length() + " caracteres.",
        "Um dos nomes mais curtos é " + nomeCurto
                + ", com " + nomeCurto.length() + " caracteres.",
        "A lista do IBGE para " + uf.getNome() + " contém "
                + municipiosDaUF.size() + " registro(s) de município."
    };

    Object[] botoes = {"Outra curiosidade", criarBotaoFechar()};
    int indice = 0;

    while (true) {
        String mensagem = "UF: " + uf.getNome() + "\n\n"
                + curiosidades[indice] + "\n\n"
                + "Curiosidade " + (indice + 1) + " de " + curiosidades.length;

        int escolha = JOptionPane.showOptionDialog(
                this,
                mensagem,
                "Curiosidades da UF",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                botoes,
                botoes[0]
        );

        if (escolha != 0) {
            break;
        }

        indice = (indice + 1) % curiosidades.length;
    }
});
        setContentPane(painel);
        java.awt.Color fundo = new java.awt.Color(235, 243, 250);
java.awt.Color azulEscuro = new java.awt.Color(27, 67, 108);
java.awt.Color azulClaro = new java.awt.Color(214, 231, 247);
java.awt.Color branco = java.awt.Color.WHITE;

painel.setBackground(fundo);
cabecalho.setBackground(fundo);
topo.setBackground(fundo);
linhaBusca.setBackground(fundo);
rodape.setBackground(fundo);

campoUF.setBackground(branco);
campoUF.setForeground(azulEscuro);
campoBusca.setBackground(branco);
campoBusca.setForeground(azulEscuro);

municipios.setBackground(branco);
municipios.setForeground(azulEscuro);
municipios.setSelectionBackground(azulEscuro);
municipios.setSelectionForeground(branco);

listaSugestoes.setBackground(branco);
listaSugestoes.setForeground(azulEscuro);
listaSugestoes.setSelectionBackground(azulClaro);
listaSugestoes.setSelectionForeground(azulEscuro);

carregar.setBackground(azulEscuro);
carregar.setForeground(branco);
botaoCuriosidade.setBackground(azulEscuro);
botaoCuriosidade.setForeground(branco);
aplicarVermelho(botaoLimpar);
status.setForeground(azulEscuro);
        setMinimumSize(new Dimension(510, 370));
        pack();
        setLocationRelativeTo(null);

        estados.addActionListener(e -> {
    if (carregando) return;

    if ("comboBoxEdited".equals(e.getActionCommand())) return;

    Object selecao = estados.getSelectedItem();

    if (selecao instanceof UF) {
        carregarMunicipios();
        return;
    }

    if (!(selecao instanceof String texto)) return;

    String termo = normalizar(texto.strip());
    UF encontrada = null;
    int quantidade = 0;

    if (!termo.isEmpty()) {
        for (int i = 0; i < estados.getItemCount(); i++) {
            UF uf = estados.getItemAt(i);
            String nome = normalizar(uf.getNome());
            String sigla = normalizar(uf.getSigla());

            if (sigla.equals(termo) || nome.equals(termo)) {
                encontrada = uf;
                quantidade = 1;
                break;
            }

            if (nome.startsWith(termo)) {
                encontrada = uf;
                quantidade++;
            }
        }
    }

    if (quantidade == 1) {
        estados.setSelectedItem(encontrada);
        return;
    }

    estados.setSelectedIndex(-1);
    municipiosDaUF = List.of();
    campoBusca.setText("");
    modelo.clear();
    campoBusca.setEnabled(false);

    if (quantidade > 1) {
        status.setText("Mais de uma UF começa assim. Digite mais letras ou a sigla.");
    } else {
        status.setText("UF não encontrada. Digite uma sigla, como PR, ou escolha na lista.");
    }

    estados.getEditor().getEditorComponent().requestFocusInWindow();
});
        carregar.addActionListener(e -> {
            if (estados.getItemCount() == 0) carregarUFs();
            else carregarMunicipios();
        
        });
        municipios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        municipios.addListSelectionListener(e -> {
            Municipio selecionado = municipios.getSelectedValue();
            if (!e.getValueIsAdjusting() && selecionado != null) {
                municipios.clearSelection();
                mostrarDetalhes(selecionado);
            }
        });
        campoBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent evento) {
                aplicarFiltro();
            }

            @Override
            public void removeUpdate(DocumentEvent evento) {
                aplicarFiltro();
            }

            @Override
            public void changedUpdate(DocumentEvent evento) {
                aplicarFiltro();
            }
        });
        carregarUFs();
    }

    private void carregarUFs() {
        executar("Carregando UFs…", consulta::buscarUFs, ufs -> {
            for (UF uf : ufs) estados.addItem(uf);
            estados.setSelectedIndex(-1);
            status.setText(ufs.isEmpty() ? "Nenhuma UF retornada. Clique em Carregar para tentar novamente."
                    : "Selecione uma UF para consultar seus municípios.");
        });
    }
    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    private void aplicarFiltro() {
        String termo = normalizar(campoBusca.getText().strip());
        modelo.clear();

        for (Municipio municipio : municipiosDaUF) {
            String nome = normalizar(municipio.getNome());

            if (nome.contains(termo)) {
                modelo.addElement(municipio);
            }
        }

        int encontrados = modelo.size();
        int total = municipiosDaUF.size();
        String contador = "Mostrando " + encontrados + " de " + total + " municípios.";

        if (termo.isEmpty() && estados.getSelectedItem() instanceof UF uf) {
    status.setText(uf.getNome() + " possui " + total + " municípios.");
} else if (encontrados == 0) {
            status.setText("Nenhum município encontrado. " + contador);
        } else {
            status.setText(contador);
        }
    }

    private void carregarMunicipios() {
    UF uf = (UF) estados.getSelectedItem();
    if (uf == null) return;

    campoBusca.setEnabled(false);
    municipiosDaUF = List.of();
    campoBusca.setText("");
    modelo.clear();

    executar("Consultando municípios de " + uf.getNome() + "…",
            () -> consulta.buscarMunicipios(uf),
            itens -> {
                municipiosDaUF = itens;
                aplicarFiltro();
                campoBusca.setEnabled(!itens.isEmpty());
            });
}

    private <T> void executar(String mensagem, Callable<List<T>> busca, Consumer<List<T>> aoConcluir) {
        carregando = true;
        estados.setEnabled(false);
        carregar.setEnabled(false);
        status.setText(mensagem);
        new SwingWorker<List<T>, Void>() {
            @Override
            protected List<T> doInBackground() throws Exception {
                return busca.call();
            }

            @Override
            protected void done() {
                try {
                    aoConcluir.accept(get());
                } catch (ExecutionException ex) {
                    String motivo = ex.getCause() instanceof IllegalStateException
                            ? ex.getCause().getMessage() : "Resposta do IBGE em formato inesperado.";
                    status.setText(motivo + " Clique em Carregar para tentar novamente.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    status.setText("Consulta interrompida. Tente novamente.");
                } finally {
                    carregando = false;
                    estados.setEnabled(estados.getItemCount() > 0);
                    carregar.setEnabled(true);
                }
            }
        }.execute();
    }
private void aplicarVermelho(JButton botao) {
    botao.setBackground(new java.awt.Color(166, 61, 64));
    botao.setForeground(java.awt.Color.WHITE);
    botao.setContentAreaFilled(true);
    botao.setOpaque(true);
}

private JButton criarBotaoFechar() {
    JButton fechar = new JButton("Fechar");
    aplicarVermelho(fechar);

    fechar.addActionListener(e -> {
        java.awt.Window janela = SwingUtilities.getWindowAncestor(fechar);
        if (janela != null) {
            janela.dispose();
        }
    });

    return fechar;
}
    private void mostrarDetalhes(Municipio municipio) {
        String[] campos = {
            "Nome: " + municipio.getNome(),
            "Código IBGE: " + municipio.getId(),
            "UF: " + municipio.getSiglaUF(),
            "Região: " + municipio.getRegiao()
        };
    
    JTextArea areaDistritos = new JTextArea(5, 24);
areaDistritos.setEditable(false);
areaDistritos.setText("Buscando distritos no IBGE...");

JScrollPane painelDistritos = new JScrollPane(areaDistritos);
painelDistritos.setBorder(BorderFactory.createTitledBorder("Distritos"));

Object[] conteudo = {campos, painelDistritos};

new SwingWorker<List<String>, Void>() {
    @Override
    protected List<String> doInBackground() {
        return consulta.buscarDistritos(municipio);
    }

    @Override
    protected void done() {
        try {
            List<String> distritos = get();

            if (distritos.isEmpty()) {
                areaDistritos.setText("Nenhum distrito para essa Localidade.");
            } else {
                areaDistritos.setText(String.join("\n", distritos));
                areaDistritos.setCaretPosition(0);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            areaDistritos.setText("A consulta foi interrompida.");
        } catch (java.util.concurrent.ExecutionException ex) {
            areaDistritos.setText("Não foi possível consultar o IBGE.\n"
                    + "Feche e abra os detalhes para tentar novamente.");
        }
    }
}.execute();    
        
        JButton fechar = criarBotaoFechar();

JOptionPane.showOptionDialog(
        this,
        conteudo,
        "Detalhes do município",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null,
        new Object[] {fechar},
        fechar
);
    }
}
