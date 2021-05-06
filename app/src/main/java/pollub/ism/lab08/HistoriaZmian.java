package pollub.ism.lab08;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "HistoriaZmianProduktow")
public class HistoriaZmian {

    @PrimaryKey(autoGenerate = true)
    public int _id;
    public int _id_warzywa;
    public String data;
    public String czas;
    public int staraIlosc;
    public int nowaIlosc;

//    public String wyswietlRekord(){
//        String staraIloscSTRING = Integer.toString(staraIlosc);
//        String nowaIloscSTRING = Integer.toString(nowaIlosc);
//
//        String string = data + ", " + czas + ", " + staraIloscSTRING + " -> " + nowaIloscSTRING + "\n";
//        return string;
//    }
}
