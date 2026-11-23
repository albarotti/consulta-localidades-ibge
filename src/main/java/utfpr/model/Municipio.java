package utfpr.model;

public final class Municipio {
    private final int id;
    private final String nome;
    private final UF uf;

    public Municipio(int id, String nome, UF uf) {
        this.id = id;
        this.nome = nome;
        this.uf = uf;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getSiglaUF() { return uf.getSigla(); }
    public String getRegiao() { return uf.getRegiao(); }

    @Override
    public String toString() { return nome; }
}
