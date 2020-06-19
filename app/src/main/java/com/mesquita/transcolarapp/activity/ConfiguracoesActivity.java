package com.mesquita.transcolarapp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mesquita.transcolarapp.R;
import com.mesquita.transcolarapp.config.Util;
import com.mesquita.transcolarapp.model.Usuario;

public class ConfiguracoesActivity extends AppCompatActivity {
    private EditText nome;
    private EditText endereco;
    private Usuario usr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        getSupportActionBar().setTitle("Configurações");

        nome = findViewById(R.id.etNome);
        endereco = findViewById(R.id.etEndereco);

        //GET EXTRA...
        usr = (Usuario) getIntent().getSerializableExtra("usr");
        if (usr != null){
            nome.setText(usr.getNome());
            endereco.setText(usr.getEndereco());
        }
    }

    public void gravar(View view) {
        if (!nome.getText().toString().isEmpty()){
            if (!endereco.getText().toString().isEmpty()) {
                usr.setNome(nome.getText().toString());
                usr.setEndereco(endereco.getText().toString());
                usr.salvar();

                Util.esconderTeclado(view);

                Toast.makeText(ConfiguracoesActivity.this,
                        "Dados salvos com sucesso!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
