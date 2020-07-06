package com.mesquita.transcolarapp.model;

import com.google.firebase.database.DatabaseReference;
import com.mesquita.transcolarapp.config.ConfiguracaoFirebase;

public class Aluno extends Usuario {
    private String matricula = "123456789";

    public Aluno() {
        super();
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public void salvar(){
        //super.salvar();

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference AlunoRef = firebaseRef.child("alunos").child(getId());

        AlunoRef.setValue(this);
    }

}
