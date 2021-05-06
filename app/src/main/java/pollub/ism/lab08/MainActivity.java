package pollub.ism.lab08;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Dao;
import androidx.room.Room;

import java.text.SimpleDateFormat;
import java.util.Date;

import pollub.ism.lab08.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ArrayAdapter<CharSequence> adapter;

    private String wybraneWarzywoNazwa = null;
    private Integer wybraneWarzywoIlosc = null;

    public enum OperacjaMagazynowa {SKLADUJ, WYDAJ};

    private BazaMagazynowa bazaDanych;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = ArrayAdapter.createFromResource(this, R.array.Asortyment, android.R.layout.simple_dropdown_item_1line);
        binding.spinner.setAdapter(adapter);

        // Podłączenie do bazy danych
        bazaDanych = Room.databaseBuilder(getApplicationContext(), BazaMagazynowa.class, BazaMagazynowa.NAZWA_BAZY)
               .allowMainThreadQueries().build();

        // W razie błędu i przy niepowodzeniu uruchomieniu aplikacji
//        bazaDanych = Room.databaseBuilder(getApplicationContext(), BazaMagazynowa.class, BazaMagazynowa.NAZWA_BAZY)
//                .allowMainThreadQueries().fallbackToDestructiveMigration().build();

        if(bazaDanych.pozycjaMagazynowaDAO().size() == 0){
            String[] asortyment = getResources().getStringArray(R.array.Asortyment);
            for(String nazwa : asortyment){
                PozycjaMagazynowa pozycjaMagazynowa = new PozycjaMagazynowa();
                pozycjaMagazynowa.NAME = nazwa; pozycjaMagazynowa.QUANTITY = 0;
                bazaDanych.pozycjaMagazynowaDAO().insert(pozycjaMagazynowa);
            }
        }

        // Listenery
        binding.przyciskSkladuj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zmienStan(OperacjaMagazynowa.SKLADUJ);
            }
        });

        binding.przyciskWydaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zmienStan(OperacjaMagazynowa.WYDAJ);
            }
        });

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                wybraneWarzywoNazwa = adapter.getItem(i).toString(); // <---
                aktualizuj();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Nie będziemy implementować, ale musi być
            }
        });
    }

    // Metoda do aktualizacji napisów
    private void aktualizuj(){

        wybraneWarzywoIlosc = bazaDanych.pozycjaMagazynowaDAO().findQuantityByName(wybraneWarzywoNazwa);

        // Dane potrzebne do zapisu historii
        int idWybranegoWarzywa = bazaDanych.pozycjaMagazynowaDAO().idWarzywaPoNazwie(wybraneWarzywoNazwa);
        HistoriaZmian zmianyWarzywa[] = bazaDanych.historiaZmianDAO().pobierzHistorieZmianWarzywa(idWybranegoWarzywa);


        if (wybraneWarzywoIlosc != 0){
            binding.tekstStanMagazynu.setText("Stan magazynu dla " + wybraneWarzywoNazwa + " wynosi " + wybraneWarzywoIlosc);
        } else {
            binding.tekstStanMagazynu.setText("Aktualnie nie mamy " + wybraneWarzywoNazwa + " :(");
        }

        // Wyświetlenie informacji o zmianach
        binding.informacje.setText("");
        for(HistoriaZmian zw: zmianyWarzywa){

            String nowaIloscSTRING = Integer.toString(zw.nowaIlosc);
            String staraIloscSTRING = Integer.toString(zw.staraIlosc);
            String string = zw.data + ", " + zw.czas + ", " + staraIloscSTRING + " -> " + nowaIloscSTRING + "\n";

            binding.informacje.append(string);
        }

        // Aktualizacja etykietki z datą
        Date data = new Date();
        String godzinaFormat = "HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(godzinaFormat);
        binding.tekstData.setText(simpleDateFormat.format(data));
    }

    // Odczytanie wartości wpisanej przez użytkownika
    private void zmienStan(OperacjaMagazynowa operacja){

        Integer zmianaIlosci = null, nowaIlosc = null;

        try {
            zmianaIlosci = Integer.parseInt(binding.edycjaIlosc.getText().toString());
        }catch(NumberFormatException ex){
            return;
        }finally {
            binding.edycjaIlosc.setText("");
        }

        // Pobranie informacji o starej ilości
        Integer staraIlosc = null;
        staraIlosc = wybraneWarzywoIlosc;

        // Rozróżnienie operacji do wykonania
        switch (operacja){
            case SKLADUJ:
                nowaIlosc = wybraneWarzywoIlosc + zmianaIlosci;
                break;

            case WYDAJ:
                nowaIlosc = wybraneWarzywoIlosc - zmianaIlosci;
                break;
        }

        // Jeżeli ilość wychodzi ujemna to nic nie rób
        if(nowaIlosc < 0){

            // Komunikat
            Toast.makeText(this, "Brak wystarczającej ilości produktów", Toast.LENGTH_LONG).show();

        // Ilośc dodatnia, następuje zmiana, aktualizacja historii
        } else {
            // Wprowadzenie zmian
            bazaDanych.pozycjaMagazynowaDAO().updateQuantityByName(wybraneWarzywoNazwa,nowaIlosc);

            // Zmiana historii
            HistoriaZmian historiaZmian = new HistoriaZmian();
            historiaZmian._id_warzywa = bazaDanych.pozycjaMagazynowaDAO().idWarzywaPoNazwie(wybraneWarzywoNazwa);

            Date date = new Date();
            String dateFormat = "dd-MM-yyyy";
            String timeFormat = "HH:mm:ss";

            // Pobranie aktualnej daty
            SimpleDateFormat simpleDateFormat_data = new SimpleDateFormat(dateFormat);
            String data = simpleDateFormat_data.format(date);

            // Pobranie aktualnego czasu
            SimpleDateFormat simpleDateFormat_czas = new SimpleDateFormat(timeFormat);
            String czas = simpleDateFormat_czas.format(date);


            // Zapisanie do obiektu
            historiaZmian.data = data;
            historiaZmian.czas = czas;
            historiaZmian.staraIlosc = staraIlosc;
            historiaZmian.nowaIlosc = nowaIlosc;

            // Obiekt zapisany do bazy danych
            bazaDanych.historiaZmianDAO().insert(historiaZmian);

        }

        aktualizuj();
    }
}