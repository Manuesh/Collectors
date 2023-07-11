package it.collectors.business.jdbc;

import com.mysql.cj.PreparedQuery;
import com.mysql.cj.x.protobuf.MysqlxPrepare;
import it.collectors.model.Collezione;
import it.collectors.model.Disco;
import it.collectors.model.Traccia;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Query_JDBC {

    private Connection connection;
    private boolean supports_procedures;
    private boolean supports_function_calls;

    public Query_JDBC(Connection c) throws ApplicationException {
        connect(c);
    }

    public final void connect(Connection c) throws ApplicationException {
        disconnect();
        this.connection = c;
        //verifichiamo quali comandi supporta il DBMS corrente
        supports_procedures = false;
        supports_function_calls = false;
        try {
            supports_procedures = connection.getMetaData().supportsStoredProcedures();
            supports_function_calls = supports_procedures && connection.getMetaData().supportsStoredFunctionsUsingCallSyntax();
        } catch (SQLException ex) {
            Logger.getLogger(Query_JDBC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void disconnect() throws ApplicationException {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                System.out.println("\n**** CHIUSURA CONNESSIONE (modulo query) ************");
                this.connection.close();
                this.connection = null;
            }
        } catch (SQLException ex) {
            throw new ApplicationException("Errore di disconnessione", ex);
        }
    }


    //********************QUERY***************************//

    // Funzionalità 22
    // ottieni collezioni utente
    public List<Collezione> getCollezioniUtente(int IDUtente){
        List<Collezione> collezioni = new ArrayList<>();
        System.out.print("ciao \n");
        try{
            CallableStatement statement = connection.prepareCall("{call get_collezioni_utente(?)}");
            statement.setInt(1,IDUtente);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            while (resultSet.next()){
                Collezione collezione = new Collezione(
                        resultSet.getInt("ID"), // ID collezione
                        resultSet.getString("nome"), // nome collezione
                        resultSet.getBoolean("flag") // flag collezione
                );
                collezioni.add(collezione);
            }
            resultSet.close();
            statement.close();

        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
        return collezioni;
    }

    // Funzionalità 21
    // ottieni ID utente
    public Integer getIDUtente(String nickname, String email){
        Integer ID = null;
        try{
            CallableStatement statement = connection.prepareCall("{call prendi_ID_utente(?,?,?)}");
            statement.setString(1, nickname);
            statement.setString(2, email);
            statement.registerOutParameter(3, Types.INTEGER);
            statement.execute();

            ID = statement.getInt(3);
            statement.close();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        System.out.println(ID);
        return ID;
    }

    // Funzionalità 18
    // convalida accesso utente
    public Boolean getAccesso(String nickname, String email){
        Boolean risultato = false;
        try {
            CallableStatement statement = connection.prepareCall("{call convalida_utente(?,?,?)}");
            statement.setString(1,nickname);
            statement.setString(2,email);
            statement.registerOutParameter(3, Types.BOOLEAN);
            statement.execute();

            risultato = statement.getBoolean(3);
            statement.close();

        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
        
        return risultato;
    }

    // Funzionalità 20
    //registrazione utente
    public boolean registrazioneUtente(String nickname, String email){
        try{
            CallableStatement statement = connection.prepareCall("{call registrazione_utente(?,?)}");
            statement.setString(1,nickname);
            statement.setString(2,email);

            statement.execute();


            statement.close();

        }catch (SQLException sqlException){
            sqlException.printStackTrace();
            return false;
        }
        return true;
    }

    // Funzionalità 16
    // aggiunta autore
    public void aggiuntaAutore(String nome, String cognome, Date dataNascita, String nomeAutore, String info, String ruolo){
        try {
            CallableStatement statement = connection.prepareCall("{call aggiunta_autore(?,?,?,?,?,?)}");
            statement.setString(1, nome);
            statement.setString(2, cognome);
            statement.setDate(3, new java.sql.Date(dataNascita.getTime()));
            statement.setString(4, nomeAutore);
            statement.setString(5, info);
            statement.setString(6, ruolo);

            statement.execute();
            statement.close();
        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
    }

    // Funzionalità 17
    // aggiunta genere
    public void aggiuntaGenere(String genere){
        try {
            CallableStatement statement = connection.prepareCall("{call aggiunta_genere(?)}");
            statement.setString(1, genere.toLowerCase());

            statement.execute();
            statement.close();

        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
    }

    // Funzionalità 18
    // rimozione genere
    public void rimozioneGenere(int id){
        try{
            CallableStatement statement = connection.prepareCall("{call rimozione_genere(?)}");
            statement.setInt(1,id);
             statement.execute();
             statement.close();

        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
    }

    // inserimento collezione
    // Funzionalità 1
    public void inserimentoCollezione(String nome, boolean flag, int IDCollezionista){
        try{
            CallableStatement statement = connection.prepareCall("{call inserisci_collezione(?,?,?)}");
            statement.setString(1,nome);
            statement.setBoolean(2,flag);
            statement.setInt(3,IDCollezionista);

            statement.execute();
            statement.close();

        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }

    }

    // Funzionalità 2

    // aggiunta di dischi a una collezione

    public void inserimentoDiscoInCollezione(int IDDisco, int IDCollezione){
        try{
            CallableStatement statement = connection.prepareCall("{call inserisci_disco_collezione(?,?)}");
            statement.setInt(1,IDDisco);
            statement.setInt(2,IDCollezione);

            statement.execute();
            statement.close();
        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
    }

    // aggiunta di tracce ad un disco
    public void inserimentoTracceInDisco(int IDDisco, String titolo, int durataOre, int durataMinuti, int durataSecondi) {
        try {
            CallableStatement statement = connection.prepareCall("{call inserisci_tracce_disco(?,?,?)}");
            statement.setInt(1, IDDisco);
            statement.setString(2, titolo);
            statement.setTime(3, new java.sql.Time(durataOre, durataMinuti, durataSecondi));

            statement.execute();
            statement.close();
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }


    // Funzionalità 3
    // Modifica dello stato di pubblicazione di una collezione

    public void modificaFlagCollezione(int IDCollezione, boolean flag){

        try{
            CallableStatement statement = connection.prepareCall("{call modifica_flag_collezione(?,?)}");
            statement.setInt(1,IDCollezione);
            statement.setBoolean(2,flag);
            statement.execute();
            statement.close();

        }catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    // Funzionalità 15
    // Aggiunta di nuove condivisioni a una collezione

    public void inserisciCondivisione(int IDCollezione, int IDCollezionista){
        try{
            CallableStatement statement = connection.prepareCall("{call inserisci_condivisione(?,?)}");
            statement.setInt(1,IDCollezione);
            statement.setInt(2,IDCollezionista);
            statement.execute();
            statement.close();
        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
    }


    // Funzionalità 4
    // Rimozione di un disco da una collezione

    public void rimozioneDiscoCollezione(int IDCollezione, int IDDisco){
        try{
            CallableStatement statement = connection.prepareCall("{call rimozione_disco_collezione(?,?)}");

            statement.setInt(1, IDDisco);
            statement.setInt(2, IDCollezione);
            statement.execute();
            statement.close();

        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
    }

    // Funzionalità 5
    // Rimozione di una collezione

    public void rimozioneCollezione(int IDCollezione) {
        try{
            CallableStatement statement = connection.prepareCall("{call rimozione_collezione(?)}");
            statement.setInt(1,IDCollezione);

            statement.execute();
            statement.close();

        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
    }

    // Funzionalità 6
    // Ottengo tutti i dischi di una collezione
    public ArrayList<Disco> listaDischiCollezione(int IDCollezione) {
        ArrayList<Disco> listaDischi = new ArrayList<Disco>();
        try {
            CallableStatement statement = connection.prepareCall("{call lista_dischi_collezione(?)}");
            statement.setInt(1, IDCollezione);
            statement.execute();

            ResultSet resultSet = statement.getResultSet();
            while(resultSet.next())
                listaDischi.add(
                        new Disco(
                                resultSet.getInt(1), //ID
                                resultSet.getString(2), // titolo
                                resultSet.getInt(3), //anno di uscita
                                resultSet.getString(4), //barcode
                                resultSet.getString(5), //formato
                                resultSet.getString(6), // stato di conservazione
                                resultSet.getString(7) // descrizione conservazione
                        )
                );
            statement.close();
            if (listaDischi.isEmpty()) return null;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return listaDischi;
    }
    // Funzionalità 7
    // Tracklist di un disco
    public ArrayList<Traccia> tracklistDisco(int IDDisco){
        ArrayList<Traccia> tracklist = new ArrayList<>();
        try {
            CallableStatement statement = connection.prepareCall("{call tracklist_disco(?)}");
            statement.setInt(1, IDDisco);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            while(resultSet.next())
                tracklist.add(
                        new Traccia(
                                resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getTime(3)
                        )
                );
            statement.close();
            if(tracklist.isEmpty()) return null;

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return tracklist;
    }

    // Funzionalità 8
    // Ricerca di dischi in base al nome autore e/o titolo del disco

    public ArrayList<Disco> getRicercaDischiPerAutoreTitolo(String nomeAutore, String titoloDisco, boolean flag, int IDCollezionista) {
        ArrayList<Disco> dischi = new ArrayList<>();
        try{
            CallableStatement statement = connection.prepareCall("{call ricerca_dischi_per_autore_titolo(?,?,?,?)}");
            statement.setString(1, nomeAutore);
            statement.setString(2, titoloDisco);
            statement.setBoolean(3, flag);
            statement.setInt(4, IDCollezionista);
            statement.execute();

            ResultSet resultSet = statement.getResultSet();
            while(resultSet.next()) {
                Disco disco = new Disco(
                        resultSet.getInt(1), //ID
                        resultSet.getString(2), // titolo
                        resultSet.getInt(3), //anno di uscita
                        resultSet.getString(4), //barcode
                        resultSet.getString(5), //formato
                        resultSet.getString(6), // stato di conservazione
                        resultSet.getString(7) // descrizione conservazione
                );
                dischi.add(disco);
            }
            statement.close();
            if(dischi.isEmpty()) return null;
        }
        catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
        return dischi;
    }

    // Funzionalità 9
    // Verifica della visibilità di una collezione da parte di un collezionista

    public boolean getVerificaVisibilitaCollezione(int IDCollezione, int IDCollezionista){
        boolean risultato = false;
        try{
            PreparedStatement statement = connection.prepareStatement("select verifica_visibilita_collezione(?,?)");
            statement.setInt(1,IDCollezione);
            statement.setInt(2,IDCollezionista);
            statement.execute();
            ResultSet rs = statement.getResultSet();

            if(rs.next()){
                 risultato = rs.getBoolean(1);
            }
            statement.close();
         }catch (SQLException sqlException){
        sqlException.printStackTrace();
        }
        return risultato;

        // ritorna falso anche se la tabella restituita non esiste
    }

    // Funzionalità 10
    // numero di tracce di dischi distinti di un certo autore presenti nelle collezioni pubbliche


    public Integer getNumeroTracceDistintePerAutoreCollezioniPubblice(int IDAutore){
        Integer risultato = null;
        try{
            CallableStatement statement = connection.prepareCall("{call numero_tracce_distinte_per_autore_collezioni_pubbliche(?,?)}");
            statement.setInt(1,IDAutore);
            statement.registerOutParameter(2, Types.INTEGER);
            statement.execute();
            risultato =  statement.getInt(2);
            statement.close();

        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
        return risultato;
    }

    // Funzionalità 11
    // minuti totali di musica riferibili a un certo autore memorizzati nelle collezioni pubbliche
    public int getMinutiTotaliMusicaPerAutore(int IDAutore){
        int minuti = 0;
        try{
            PreparedStatement statement = connection.prepareStatement("select minuti_totali_musica_pubblica_per_autore(?)");
            statement.setInt(1, IDAutore);
            statement.execute();

            ResultSet resultset = statement.getResultSet();
            if (resultset.next())
            {
                minuti = resultset.getInt(1);
            }

            statement.close();

        }catch(SQLException sqlException){
            sqlException.printStackTrace();
        }
        return minuti;
    }

    // Funzionalità 12
    // statistiche: numero collezioni di ciascun collezionista
    public int getStatisticheNumeroCollezioni() {
        try {
            CallableStatement statement = connection.prepareCall("{call statistiche_numero_collezioni(?)}");
            statement.registerOutParameter(1, Types.INTEGER);
            statement.execute();
            return statement.getInt(1);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return 0;
    }
    // statistiche: numero di dischi per genere nel sistema
    public int getStatisticheDischiPerGenere(){
        try {
            CallableStatement statement = connection.prepareCall("{call statistiche_dischi_per_genere(?)}");
            statement.registerOutParameter(1,Types.INTEGER);
            statement.execute();
            return statement.getInt(1);

        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
        return 0;
    }

    // Funzionalità 13
    public HashMap<Disco, Integer> dischiSimiliA(String barcode, String titolo, String autore){
        HashMap<Disco, Integer> dischi = new HashMap<>();
        try {
            PreparedStatement queryBarcode = connection.prepareStatement("select * from disco where barcode like ?");
            PreparedStatement queryTitolo = connection.prepareStatement("select * from disco where titolo like ?");
            PreparedStatement queryAutore = connection.prepareStatement(
                        "select d.* from " +
                            "disco d join produce_disco p on d.ID=p.ID_disco" +
                            "join autore a on p.ID_autore=a.ID" +
                            "where nome_autore like ?"
            );

            queryBarcode.setString(1, barcode);
            queryTitolo.setString(1, titolo);
            queryAutore.setString(1, autore);



            ResultSet barcodeResult = queryBarcode.executeQuery();
            ResultSet titoloResult = queryTitolo.executeQuery();
            ResultSet autoreResult = queryAutore.executeQuery();

            while(barcodeResult.next()) {
                dischi.put(
                        new Disco(
                                barcodeResult.getInt(1), //ID
                                barcodeResult.getString(2), // titolo
                                barcodeResult.getInt(3), //anno di uscita
                                barcodeResult.getString(4), //barcode
                                barcodeResult.getString(5), //formato
                                barcodeResult.getString(6), // stato di conservazione
                                barcodeResult.getString(7) // descrizione conservazione
                        ), 1
                );
            }

            while(titoloResult.next()){
                Disco disco = new Disco(

                        titoloResult.getInt(1), //ID
                        titoloResult.getString(2), // titolo
                        titoloResult.getInt(3), //anno di uscita
                        titoloResult.getString(4), //barcode
                        titoloResult.getString(5), //formato
                        titoloResult.getString(6), // stato di conservazione
                        titoloResult.getString(7) // descrizione conservazione
                );

                if (dischi.containsKey(disco))
                    dischi.put(disco, dischi.get(disco) + 1);
                else
                    dischi.put(disco, 1);
            }

            while(autoreResult.next()){
                Disco disco = new Disco(
                        autoreResult.getInt(1), //ID
                        autoreResult.getString(2), // titolo
                        autoreResult.getInt(3), //anno di uscita
                        autoreResult.getString(4), //barcode
                        autoreResult.getString(5), //formato
                        autoreResult.getString(6), // stato di conservazione
                        autoreResult.getString(7) // descrizione conservazione
                );
                if(dischi.containsKey(disco))
                    dischi.put(disco, dischi.get(disco) + 1);
                else
                    dischi.put(disco, 1);
            }

            queryBarcode.close();
            queryTitolo.close();
            queryAutore.close();
            if (dischi.isEmpty()) return null;

        } catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
        return dischi;
    }

}

