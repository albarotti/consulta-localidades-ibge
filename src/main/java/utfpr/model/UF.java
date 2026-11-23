package utfpr.model;

public final class UF {
    private final int id;
    private final String nome;
    private final String sigla;
    private final String regiao;

    public UF(int id, String nome, String sigla, String regiao) {
        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
        this.regiao = regiao;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getSigla() { return sigla; }
    public String getRegiao() { return regiao; }

    @Override
    public String toString() { return nome + " (" + sigla + ")"; }
}
