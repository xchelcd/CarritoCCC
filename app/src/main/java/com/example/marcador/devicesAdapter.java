package com.example.marcador;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class devicesAdapter extends  RecyclerView.Adapter<devicesAdapter.ViewHolder> implements View.OnClickListener {

    private View.OnClickListener listener;
    private Device deviceSelected;
    public void setOnClickListener(View.OnClickListener listener){
        //deviceSelected= (Device) listener;
        //return deviceSelected;
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if(listener != null){
            listener.onClick(v);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView descripcion;
        private TextView identificador;


        Context context;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            context= itemView.getContext();
            descripcion= itemView.findViewById(R.id.name_device);
            identificador=itemView.findViewById(R.id.identificador);

            final View parent = itemView;
        }

        public void Bind(Device device){
        descripcion.setText(device.getName());
        identificador.setText(device.getId());




        }
    }

    private List<Device> devices;
    public devicesAdapter(List<Device> devices){
        this.devices = devices;
    }

    @NonNull
    @Override
    public devicesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.device, viewGroup, false);
        view.setOnClickListener(this);
        //return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.products_rc, viewGroup, false));
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull devicesAdapter.ViewHolder viewHolder, int i) {
        viewHolder.Bind(devices.get(i));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }
}
