package com.mesquita.transcolarapp.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class ConfiguracaoFirebase {
    private static DatabaseReference database;
    private static FirebaseAuth autenticacao;
    private static FirebaseInstanceId instanceId;

    //retorna a instância do FirabaseDatabase
    public static DatabaseReference getFirebaseDatabase(){
        if (database == null) {
            database = FirebaseDatabase.getInstance().getReference();
        }
        return database;
    }

    //retorna a instância do FirebaseAuth
    public static FirebaseAuth getFirebaseAutenticacao(){
        if (autenticacao == null) {
            autenticacao = FirebaseAuth.getInstance();
        }
        return autenticacao;
    }

    //retorna a instância do FirebaseInstanceID
    public static FirebaseInstanceId getFirebaseInstanceId(){
        if (instanceId == null) {
            instanceId = FirebaseInstanceId.getInstance();
        }
        return instanceId;
    }

}
