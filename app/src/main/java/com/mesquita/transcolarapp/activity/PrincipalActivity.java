package com.mesquita.transcolarapp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mesquita.transcolarapp.R;
import com.mesquita.transcolarapp.config.ConfiguracaoFirebase;
import com.mesquita.transcolarapp.model.Motorista;
import com.mesquita.transcolarapp.model.Responsavel;
import com.mesquita.transcolarapp.model.Usuario;
import com.mesquita.transcolarapp.services.TrackUserLocationService;

public class PrincipalActivity extends AppCompatActivity {
    private FirebaseAuth autenticacao;
    private TextView ola;
    private Usuario usr;
    private Usuario aux;
    private Motorista mot;
    private TrackUserLocationService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        ola = findViewById(R.id.tvOla);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        usr = new Usuario();
        usr.setId(autenticacao.getUid());

        buscarUsuario();
    }

    public void buscarUsuario(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        final DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(usr.getId());

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usr = dataSnapshot.getValue(Usuario.class);
                ola.setText("Olá, " + usr.getNome());

                if(usr.getTipoUsuario().equals("motorista")) {
                    mot = buscarMotorista();
                }

                //Primeira implementação
                //Utilizado para testes já que ainda não existe um método que inicia a ida da criança até a escola
                if(usr.getTipoUsuario().equals("aluno"))
                {
                    Intent intent = new Intent(getApplicationContext(), TrackUserLocationService.class);
                    Bundle b = new Bundle();
                    LatLng mesquitaSchoolLocation = new LatLng(-30.011553, -51.153241);
                    b.putParcelable("schoolLocation", mesquitaSchoolLocation);
                    intent.putExtras(b);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public Motorista buscarMotorista() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        final DatabaseReference motoristaRef = firebaseRef.child("motoristas").child(usr.getId());

        motoristaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mot = snapshot.getValue(Motorista.class);
                for (Responsavel r: mot.getClientes()) {
                    buscarLatLng(r);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mot = null;
            }
        });

        return mot;
    }

    public void buscarLatLng(final Responsavel resp) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        Log.i("usuario - resp 0", resp.getId());

        final DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(resp.getId());

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                aux = snapshot.getValue(Usuario.class);
                resp.setLatitude(aux.getLatitude());
                resp.setLongitude(aux.getLongitude());
                Log.i("usuario - resp 0", resp.getLatitude().toString());
                Log.i("usuario - resp 0", resp.getLongitude().toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

                //Realiza a conexão do serviço quando necessário.
                //Futuramente deve ser movido para uma classe especifica de ações sobre o aluno
        private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            TrackUserLocationService.TrackUserLocationServiceBinder binder = (TrackUserLocationService.TrackUserLocationServiceBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.itemSair) {
            ConfiguracaoFirebase.getFirebaseAutenticacao().signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void configurarUser(View view){
        Intent i;
        if (usr.getTipoUsuario().equals("motorista")){
            i = new Intent(this, ConfiguracoesMotoristaActivity.class);
        } else if (usr.getTipoUsuario().equals("responsavel")) {
            i = new Intent(this, ConfiguracoesResponsavelActivity.class);
        } else {
            i = new Intent(this, ConfiguracoesAlunoActivity.class);
        }

        i.putExtra("usr", usr);
        startActivity(i);
    }

    public void abrirMapa(View view) {
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("mot", mot);
        startActivity(i);
    }
}
