package phpeterle.view;

import de.micromata.opengis.kml.v_2_2_0.*;
import phpeterle.modelos.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class KmlFile {

    String[] mColors = {
            "8f39add1", // light blue
            "8f3079ab", // dark blue
            "8fc25975", // mauve
            "8fe15258", // red
            "8ff9845b", // orange
            "8f838cc7", // lavender
            "8f7d669e", // purple
            "8f53bbb4", // aqua
            "8f51b46d", // green
            "8fe0ab18", // mustard
            "8f637a91", // dark gray
            "8ff092b0", // pink
            "8fb7c0c7"  // light gray
    };


    public void criarArquivo(List<Formiga> melhorCaminho, String arquivo) throws IOException {
        final Kml kml = new Kml();
        Document doc = kml.createAndSetDocument().withName("TCC").withOpen(true);

        // create a Folder
        Folder folder = doc.createAndAddFolder();
        folder.withName("Continents with Earth's surface").withOpen(true);



        for (int i = 0; i < melhorCaminho.size(); i ++) {
            Formiga formiga = melhorCaminho.get(i);

            Style style = doc.createAndAddStyle();
            style.withId("style_" + i) // set the stylename to use this style from the placemark
                    .createAndSetPolyStyle().withColor(mColors[i]); // set size and icon

            Placemark placemark = doc.createAndAddFolder().withName("Caminhao " + i).createAndAddPlacemark().withStyleUrl("#style_" + i);
            placemark.withOpen(true);

            LinearRing linearRing = new LinearRing();

            for (int j = 0; j < formiga.cidadesVisitadas.size(); j++) {
                Localidade localidade = formiga.cidadesVisitadas.get(j);
                linearRing.addToCoordinates(localidade.getY(), localidade.getX());
                createPlacemarkWithChart(doc, folder, localidade.getY(), localidade.getX(), localidade.getNome(), localidade.hotel ? "sleep" : "");
            }

            placemark.createAndSetMultiGeometry().createAndAddPolygon().withTessellate(true).createAndSetOuterBoundaryIs().setLinearRing(linearRing);

        }

        // print and save
        kml.marshal(new File(arquivo + ".kml"));
    }

    private void createPlacemarkWithChart(Document document, Folder folder, double longitude, double latitude,
                                                 String continentName, String styleId) {
        Style style = document.createAndAddStyle();
        style.withId("style_icon_").createAndSetIconStyle().withScale(1).withColor("ff43b3ff");

        Style blackStyle = document.createAndAddStyle();
        style.withId("style_icon_sleep").createAndSetIconStyle().withScale(1).withColor("ff000000");

        Placemark placemark = folder.createAndAddPlacemark();
        // use the style for each continent
        placemark.withName(continentName)
                .withStyleUrl("#style_icon_" + styleId)
                // 3D chart imgae
                .createAndSetLookAt().withLongitude(longitude).withLatitude(latitude);

        placemark.createAndSetPoint().addToCoordinates(longitude, latitude); // set coordinates
    }
}
