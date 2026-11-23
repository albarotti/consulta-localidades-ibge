package utfpr.converter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;
import utfpr.model.Municipio;
import utfpr.model.UF;

public class ConversorLocalidades {
    public List<UF> converterUFs(String json) {
        JSONArray dados = new JSONArray(json);
        List<UF> ufs = new ArrayList<>();
        for (int i = 0; i < dados.length(); i++) {
            JSONObject item = dados.getJSONObject(i);
            String regiao = item.getJSONObject("regiao").getString("nome");
            ufs.add(new UF(item.getInt("id"), item.getString("nome"), item.getString("sigla"), regiao));
        }
        ufs.sort(Comparator.comparing(UF::getNome, Collator.getInstance(Locale.forLanguageTag("pt-BR"))));
        return ufs;
    }

    public List<Municipio> converterMunicipios(String json, UF uf) {
        JSONArray dados = new JSONArray(json);
        List<Municipio> municipios = new ArrayList<>();
        for (int i = 0; i < dados.length(); i++) {
            JSONObject item = dados.getJSONObject(i);
            municipios.add(new Municipio(item.getInt("id"), item.getString("nome"), uf));
        }
        municipios.sort(Comparator.comparing(Municipio::getNome, Collator.getInstance(Locale.forLanguageTag("pt-BR"))));
        return municipios;
    }
}
