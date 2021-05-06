package pollub.ism.lab08;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface HistoriaZmianDAO {

    // Wstawienie rekordu
    @Insert
    public void insert(HistoriaZmian historiaZmian);

    // Zwrócenie wszystkich rekordów dot. danego warzywa
    @Query("SELECT * FROM HistoriaZmianProduktow WHERE _id_warzywa = :idWybranegoWarzywa")
    HistoriaZmian[] pobierzHistorieZmianWarzywa(int idWybranegoWarzywa);
}
