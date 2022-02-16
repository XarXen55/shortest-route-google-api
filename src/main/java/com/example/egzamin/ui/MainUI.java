package com.example.egzamin.ui;

import com.example.egzamin.DistanceMatrix;
import com.example.egzamin.Geocoder;
import com.example.egzamin.TspCities;
import com.example.egzamin.VrpTimeWindows;
import com.example.egzamin.models.Locations;
import com.example.egzamin.models.LocationsSorted;
import com.example.egzamin.repository.LocationsRepository;
import com.example.egzamin.repository.LocationsSortedRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.internal.Pair;
import com.vaadin.flow.router.Route;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Route("/ui/menu")
public class MainUI extends VerticalLayout {

    @Autowired
    LocationsRepository locationsRepository;

    @Autowired
    LocationsSortedRepository locationsSortedRepository;

    HorizontalLayout underGridLayout = new HorizontalLayout();
    HorizontalLayout destinationLayout = new HorizontalLayout();
    VerticalLayout verticalLayoutLeft = new VerticalLayout();
    VerticalLayout verticalLayoutRight = new VerticalLayout();
    HorizontalLayout horizontalLayout = new HorizontalLayout();

    private TextField location = new TextField("Location");

    private TextField destinationCity = new TextField("Miasto");
    private TextField destinationRoad = new TextField("Ulica");
    private TextField destinationBuildingNumber = new TextField("Numer budynku");


    //private Checkbox BackToFirstLocation = new Checkbox("Back To First Location");

    boolean CalculateTime,CalculateKM;

    //private ComboBox combo = new ComboBox("Metoda optymalizacji","CalculateKM","CalculateTime");

    private Select<String> select = new Select<>();

    private Grid<Locations> grid = new Grid<>(Locations.class,false);

    private Button button = new Button("Kalkuluj Trase", e -> {
        try {
            send();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    });
    private Button addDestinationButton;
    private Button deleteDestinationButton;


    private Label label = new Label();
    private Label lbl = new Label();

    Locations skrytka = new Locations();


    @PostConstruct
    private void init() {
        locationsRepository.deleteAll();
        locationsSortedRepository.deleteAll();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        SingleSelect<Grid<Locations>,Locations> skrytkaSingleSelect = grid.asSingleSelect();
        skrytkaSingleSelect.addValueChangeListener(e -> {
            skrytka = e.getValue();
        });

        addDestinationButton = new Button("Add destination", click -> addDestToGrid());
        deleteDestinationButton = new Button("Delete chosen destination");
        deleteDestinationButton.addClickListener(e -> deleteFromGrid());


        conf();
        selectConf();

        destinationLayout.add(destinationCity, destinationRoad, destinationBuildingNumber);
        underGridLayout.add(button, deleteDestinationButton);
        verticalLayoutLeft.add(location, destinationLayout, addDestinationButton ,select,grid,underGridLayout);
        verticalLayoutRight.add(label,lbl);
        horizontalLayout.add(verticalLayoutLeft,verticalLayoutRight);
        add(horizontalLayout);
        refresh();
    }


    private void conf() {
        grid.setMaxWidth("600px");
        grid.addColumn(e -> e.getId()).setHeader("Id").setAutoWidth(true);
        grid.addColumn(e -> e.getAdres()).setHeader("Adres");
    }
    private void selectConf(){
        select.setLabel("Metoda optymalizacji");
        select.setItems("Kilometers", "Time");
        select.setValue("Kilometers");
    }
    private void addDestToGrid(){
        Locations destLocation = new Locations(destinationCity.getValue()+" "+destinationRoad.getValue()+" "+destinationBuildingNumber.getValue());
        locationsRepository.save(destLocation);
        grid.setItems(locationsRepository.findAll());

        destinationCity.clear();
        destinationRoad.clear();
        destinationBuildingNumber.clear();
        grid.getDataProvider().refreshAll();
    }

    private void deleteFromGrid(){
        locationsRepository.delete(locationsRepository.getById(grid.asSingleSelect().getValue().getId()));
        grid.setItems(locationsRepository.findAll());
        grid.getDataProvider().refreshAll();
    }

    private void send() throws Exception {
        Geocoder geocoder = new Geocoder();
        List<String> adresses = new ArrayList<>();
        HashMap<Integer, String> adresHashMap = new HashMap<Integer, String>();

        //Get all repo in map
        int p = 1;
        for(int k = 0; k <= locationsRepository.findTopByOrderByIdDesc().getId(); k++){
            if(locationsRepository.existsById((long) k)){
                adresHashMap.put(p, locationsRepository.getById((long) k).getAdres());
                System.out.println("Hashmap ["+p+"] - "+ locationsRepository.getById((long) k).getAdres());
                p++;
            }

        }
        adresses.add(geocoder.GeocodeSync(location.getValue()));
        //locationsRepository.save(new Locations(0L,location.getValue()));
        locationsRepository.findAll().stream()
                .forEach(x -> {
                    try {
                        adresses.add(geocoder.GeocodeSync(x.getAdres()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
        DistanceMatrix distanceMatrix = new DistanceMatrix();
        if(select.getValue().equals("Kilometers"))
        {
            List<Long> longs = TspCities.CalculateMatrix(distanceMatrix.usage(adresses));
            int xdd = 0;
            for(int i = 0; i <= locationsRepository.findTopByOrderByIdDesc().getId(); i++) {
                //System.out.println("Longs: " + longs.get(i));

                if (longs.get(xdd) == 0) {
                    //System.out.println(location.getValue());
                    //locationsSortedRepository.save(new LocationsSorted(longs.get(0), location.getValue()));
                    xdd++;
                } else if (locationsRepository.existsById((long) i) && xdd<=longs.size()){
                    //System.out.println("i: " + i + ", " + " xd: " + xdd);
                    //System.out.println("id - "+longs.get(xdd)+", adres- "+adresHashMap.get(longs.get(xdd).intValue()));
                    locationsSortedRepository.save(new LocationsSorted( longs.get(xdd), adresHashMap.get(longs.get(xdd).intValue())));
                    xdd++;
                } else {
                    //System.out.println("Time No ID Exists " + i);
                }
            }
            String textForLabel = new String();
            //textForLabel = textForLabel + location.getValue() + " -> ";
            textForLabel = location.getValue() + " -> " + locationsSortedRepository.findAll().stream()
                    .map(x -> x.getAdres()).collect(Collectors.joining(" -> "));
            textForLabel = textForLabel + " -> " + location.getValue() /*+ longs.get(longs.size()-1).toString() + " kilometers"*/;
            label.setText(textForLabel);
        }
        else if(select.getValue().equals("Time"))
        {
            List<Long> longs = VrpTimeWindows.CalculateMatrixTime(distanceMatrix.usage_time(adresses));
            int xd = 0;
            for(int i = 0; i <= locationsRepository.findTopByOrderByIdDesc().getId(); i++) {
                //System.out.println("Longs: " + longs.get(i));
                if (longs.get(xd) == 0) {
                    //System.out.println(location.getValue());
                    //locationsSortedRepository.save(new LocationsSorted(longs.get(0), location.getValue()));
                    xd++;
                } else if (locationsRepository.existsById((long) i) && xd<=longs.size()){
                    //System.out.println("i: " + i + " " + locationsRepository.getById(longs.get(xd)).getAdres() + " xd: " + xd);
                    //System.out.println("id - "+longs.get(xd)+", adres- "+locationsRepository.getById(longs.get(xd)).getAdres());
                    locationsSortedRepository.save(new LocationsSorted( longs.get(xd), adresHashMap.get(longs.get(xd).intValue())));
                    xd++;
                } else {
                    //System.out.println("Time No ID Exists " + i);
                }
            }
            String textForLabel = new String();
            textForLabel = textForLabel + location.getValue() + " -> ";
            textForLabel = textForLabel + locationsSortedRepository.findAll().stream()
                    .map(x -> x.getAdres()).collect(Collectors.joining(" -> "));
            textForLabel = textForLabel + " -> " + location.getValue() /*+ " ,Total Time: " + longs.get(longs.size()-1).toString() + " minutes"*/;
            label.setText(textForLabel);
        }
        //locationsRepository.deleteById(locationsRepository.count()-1);
        String needed_waypoints = new String();
        needed_waypoints = locationsSortedRepository.findAll().stream()
                .map(x -> x.getAdres()).collect(Collectors.joining("|"));
        lbl.getElement().setProperty("innerHTML",
                "<iframe\n" +
                        "  width=\"800\"\n" +
                        "  height=\"600\"\n" +
                        "  frameborder=\"0\" style=\"border:0\"\n" +
                        "  src=\"https://www.google.com/maps/embed/v1/directions\n" +
                        "?key=AIzaSyDuC0IURK2hq4tMoTu61F_XKqI1YWRleLY\n" +
                        "&origin=place_id:" + adresses.get(0) +
                        "&destination=place_id:" + adresses.get(0) +
                        "&waypoints=" + needed_waypoints +"\" allowfullscreen=\"\">\n" +
                        "\n" +
                        "</iframe>");
        locationsSortedRepository.deleteAll();
    }

    private void refresh()
    {

    }
}
