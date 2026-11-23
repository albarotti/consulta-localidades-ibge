package utfpr.http;

import java.util.List;
import utfpr.converter.ConversorLocalidades;
import utfpr.model.Municipio;
import utfpr.model.UF;

public class ConsultaLocalidades {
    private static final String ESTADOS = "https://servicodados.ibge.gov.br/api/v1/localidades/estados";
    private final ClienteHttp cliente = new ClienteHttp();
    private final ConversorLocalidades conversor = new ConversorLocalidades();

    public List<UF> buscarUFs() {
        return conversor.converterUFs(cliente.buscaDados(ESTADOS));
    }
public List<String> buscarDistritos(Municipio municipio) {
    String url = "https://servicodados.ibge.gov.br/api/v1/localidades/municipios/"
            + municipio.getId() + "/distritos?orderBy=nome";

    String resposta = cliente.buscaDados(url);
    org.json.JSONArray dados = new org.json.JSONArray(resposta);
    List<String> nomes = new java.util.ArrayList<>();

    for (int i = 0; i < dados.length(); i++) {
        String nome = dados.getJSONObject(i).getString("nome");
        if (!nome.strip().equalsIgnoreCase(municipio.getNome().strip())) {
    nomes.add(nome);
}
    }

    return nomes;
}
    public List<Municipio> buscarMunicipios(UF uf) {
        String url = ESTADOS + "/" + uf.getId() + "/municipios";
        return conversor.converterMunicipios(cliente.buscaDados(url), uf);
    }
}
