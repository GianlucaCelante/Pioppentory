package it.pioppi.business.fragment;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.pioppi.ConstantUtils;
import it.pioppi.R;
import it.pioppi.business.adapter.BluetoothDevicesAdapter;
import it.pioppi.business.dto.BluetoothDeviceDto;
import it.pioppi.business.dto.BluetoothSocketHolder;
import it.pioppi.business.service.BluetoothScannerService;
import it.pioppi.business.viewmodel.BluetoothDeviceViewModel;

public class OptionsFragment extends Fragment implements BluetoothDevicesAdapter.OnItemClickListener, BluetoothDevicesAdapter.OnLongItemClickListener {

    private BluetoothDevicesAdapter pairedDevicesAdapter;
    private BluetoothDevicesAdapter nearbyDevicesAdapter;
    private ExecutorService executorService;
    private BluetoothDeviceViewModel bluetoothDeviceViewModel;
    private RecyclerView pairedBluetoothDevicesRecyclerView;
    private RecyclerView nearbyBluetoothDevicesRecyclerView;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver receiver;
    private FloatingActionButton fabDiscoverDevices;

    private boolean isReceiverRegistered = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        executorService = Executors.newSingleThreadExecutor();
        bluetoothDeviceViewModel = new ViewModelProvider(requireActivity()).get(BluetoothDeviceViewModel.class);

        BluetoothManager bluetoothManager = (BluetoothManager) requireContext().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Il dispositivo non supporta il Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            checkBluetoothPermissions();
        }

    }

    private void initializeReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    updateNearbyDevices(deviceName, deviceHardwareAddress, true);

                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Toast.makeText(getContext(), "Ricerca dispositivi in corso...", Toast.LENGTH_LONG).show();
                    resetDeviceDetection();
                    if (fabDiscoverDevices != null) {
                        fabDiscoverDevices.setEnabled(false);
                        fabDiscoverDevices.setAlpha(0.5f);
                    }

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                    Toast.makeText(getContext(), "Ricerca dispositivi Bluetooth terminata", Toast.LENGTH_SHORT).show();
                    removeUnseenDevices();
                    if (fabDiscoverDevices != null) {
                        fabDiscoverDevices.setEnabled(true);
                        fabDiscoverDevices.setAlpha(1.0f);
                    }
                }
            }
        };
    }

    private void resetDeviceDetection() {
        List<BluetoothDeviceDto> currentDevices = bluetoothDeviceViewModel.getNearbyBluetoothDevices().getValue();
        if (currentDevices != null) {
            for (BluetoothDeviceDto device : currentDevices) {
                device.setDetected(false);
            }
        }
    }

    private void updateNearbyDevices(String deviceName, String deviceAddress, boolean detected) {
        BluetoothDeviceDto newDevice = new BluetoothDeviceDto();
        newDevice.setName(deviceName);
        newDevice.setAddress(deviceAddress);
        newDevice.setCreationDate(ZonedDateTime.now(ZoneId.of("Europe/Rome")));
        newDevice.setDetected(detected);

        List<BluetoothDeviceDto> currentDevices = bluetoothDeviceViewModel.getNearbyBluetoothDevices().getValue();
        if (currentDevices == null) {
            currentDevices = new ArrayList<>();
        }

        List<BluetoothDeviceDto> finalCurrentDevices = currentDevices;
        boolean deviceExists = currentDevices.stream()
                .filter(device -> device.getAddress().equals(deviceAddress))
                .findFirst()
                .map(existingDevice -> {
                    int index = finalCurrentDevices.indexOf(existingDevice);
                    finalCurrentDevices.set(index, newDevice);
                    finalCurrentDevices.get(index).setDetected(detected); // Aggiorna il flag "rilevato"
                    return true;
                })
                .orElse(false);

        if (!deviceExists) {
            currentDevices.add(newDevice);
        }

        bluetoothDeviceViewModel.setNearbyBluetoothDevices(currentDevices);
        nearbyDevicesAdapter.notifyDataSetChanged();
    }

    private void removeUnseenDevices() {
        List<BluetoothDeviceDto> currentDevices = bluetoothDeviceViewModel.getNearbyBluetoothDevices().getValue();
        if (currentDevices != null) {
            // Rimuovi tutti i dispositivi che non sono stati rilevati durante l'ultima scansione
            List<BluetoothDeviceDto> devicesToRemove = new ArrayList<>();
            for (BluetoothDeviceDto device : currentDevices) {
                if (!device.isDetected()) {
                    devicesToRemove.add(device);
                }
            }
            currentDevices.removeAll(devicesToRemove);

            // Aggiorna la lista nel ViewModel e l'adapter
            bluetoothDeviceViewModel.setNearbyBluetoothDevices(currentDevices);
            nearbyDevicesAdapter.notifyDataSetChanged();
        }
    }

    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE) != PackageManager.PERMISSION_GRANTED) {

                // Richiedi permessi per Bluetooth e foreground service collegati ai dispositivi
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE
                        }, ConstantUtils.REQUEST_BLUETOOTH_PERMISSION);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Controllo per versioni di Android M e superiori
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ConstantUtils.REQUEST_LOCATION_PERMISSION);
            }
        }
    }

    private void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getContext(), "Abilita la localizzazione per cercare i dispositivi Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    private void startBluetoothDiscovery() {

        if (!checkAndRequestPermissions()) {
            return;
        }

        try {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            boolean success = bluetoothAdapter.startDiscovery();
            if (success) {
                Log.d("BluetoothDebug", "Discovery avviata correttamente");
                if (fabDiscoverDevices != null) {
                    fabDiscoverDevices.setEnabled(false);
                    fabDiscoverDevices.setAlpha(0.5f);
                }
            } else {
                Toast.makeText(getContext(), "Impossibile avviare la ricerca", Toast.LENGTH_SHORT).show();
                if (fabDiscoverDevices != null) {
                    fabDiscoverDevices.setEnabled(true);
                    fabDiscoverDevices.setAlpha(1.0f);
                }
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        fetchPairedDevices();
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
                if (fabDiscoverDevices != null) {
                    fabDiscoverDevices.setEnabled(true);
                    fabDiscoverDevices.setAlpha(1.0f);
                }
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if (isReceiverRegistered) {
            requireContext().unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ConstantUtils.REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth();
                fetchPairedDevices();
            } else {
                Toast.makeText(getContext(), "Permesso Bluetooth negato", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == ConstantUtils.REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth();
            } else {
                Toast.makeText(getContext(), "Permesso di localizzazione negato", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ConstantUtils.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // Il Bluetooth è abilitato
                fetchPairedDevices();
            } else {
                // L'utente ha rifiutato di abilitare il Bluetooth
                Toast.makeText(getContext(), "Bluetooth non abilitato", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == ConstantUtils.REQUEST_DISCOVERABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // L'utente ha rifiutato di rendere il dispositivo visibile
                Toast.makeText(getContext(), "Visibilità Bluetooth non abilitata", Toast.LENGTH_SHORT).show();
            } else {
                // Il dispositivo è ora visibile per resultCode secondi
                Toast.makeText(getContext(), "Il dispositivo è ora visibile per " + resultCode + " secondi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ConstantUtils.REQUEST_ENABLE_BT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_options, container, false);
        initializeReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        if (!isReceiverRegistered) {  // Verifica se il receiver è già registrato
            requireContext().registerReceiver(receiver, filter);
            isReceiverRegistered = true;  // Imposta il flag come true
        }

        pairedBluetoothDevicesRecyclerView = view.findViewById(R.id.recycler_view_devices);
        nearbyBluetoothDevicesRecyclerView = view.findViewById(R.id.recycler_view_nearby_devices);

        pairedDevicesAdapter = new BluetoothDevicesAdapter(new ArrayList<>(), this, this);
        nearbyDevicesAdapter = new BluetoothDevicesAdapter(new ArrayList<>(), this, this);

        pairedBluetoothDevicesRecyclerView.setAdapter(pairedDevicesAdapter);
        pairedBluetoothDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        nearbyBluetoothDevicesRecyclerView.setAdapter(nearbyDevicesAdapter);
        nearbyBluetoothDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bluetoothDeviceViewModel.getPairedBluetoothDevices().observe(getViewLifecycleOwner(), devices -> {
            if (pairedDevicesAdapter != null) {
                pairedDevicesAdapter.setBluetoothDevices(devices);
            }
        });

        bluetoothDeviceViewModel.getNearbyBluetoothDevices().observe(getViewLifecycleOwner(), devices -> {
            if (nearbyDevicesAdapter != null) {
                nearbyDevicesAdapter.setBluetoothDevices(devices);
            }
        });

        fabDiscoverDevices = view.findViewById(R.id.fab_discover_devices);
        fabDiscoverDevices.setOnClickListener(v -> {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                startBluetoothDiscovery();
            } else {
                Toast.makeText(getContext(), "Bluetooth non attivo", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();

        try {
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
                if (fabDiscoverDevices != null) {
                    fabDiscoverDevices.setEnabled(true);
                    fabDiscoverDevices.setAlpha(1.0f);
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if (isReceiverRegistered) {
            requireContext().unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }
    }


    @Override
    public void onItemClick(BluetoothDeviceDto bluetoothDevice) {
        if (bluetoothDevice == null || bluetoothDevice.getAddress() == null) {
            Toast.makeText(getContext(), "Dispositivo non valido", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothSocketHolder socketHolder = BluetoothSocketHolder.getInstance();
        BluetoothDevice connectedDevice = socketHolder.getConnectedDevice();

        if (connectedDevice != null && connectedDevice.getAddress().equals(bluetoothDevice.getAddress())) {
            // Il dispositivo è già connesso, quindi disconnettiamo
            disconnectFromDevice(bluetoothDevice);
        } else {
            // Connettiamo al dispositivo
            connectToDevice(bluetoothDevice);
        }
    }

    private void connectToDevice(BluetoothDeviceDto bluetoothDevice) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress());

        if (device != null) {
            // Avvia un thread per la connessione
            new Thread(() -> {
                try {
                    // Ferma la scoperta per migliorare la velocità di connessione
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }

                    // Connessione al socket RFCOMM usando UUID SPP
                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(ConstantUtils.SPP_UUID);
                    socket.connect();

                    // Memorizza il socket e il dispositivo connesso
                    BluetoothSocketHolder.getInstance().setSocket(socket);
                    BluetoothSocketHolder.getInstance().setConnectedDevice(device);

                    // Una volta connesso, aggiorna l'UI sul thread principale
                    requireActivity().runOnUiThread(() -> {
                        // Aggiorna lo stato del dispositivo
                        bluetoothDevice.setConnected(true);

                        // Sposta il dispositivo in cima alla lista
                        List<BluetoothDeviceDto> devicesList;
                        if (pairedDevicesAdapter.getBluetoothDevices().contains(bluetoothDevice)) {
                            devicesList = bluetoothDeviceViewModel.getPairedBluetoothDevices().getValue();
                        } else {
                            devicesList = bluetoothDeviceViewModel.getNearbyBluetoothDevices().getValue();
                        }

                        if (devicesList != null) {
                            devicesList.remove(bluetoothDevice);
                            devicesList.add(0, bluetoothDevice);
                        }

                        // Notifica l'adapter dei cambiamenti
                        pairedDevicesAdapter.notifyDataSetChanged();
                        nearbyDevicesAdapter.notifyDataSetChanged();

                        // Avvia il servizio
                        startBluetoothScannerService();
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    // Gestisci l'errore (mostra un messaggio all'utente)
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Errore durante la connessione al dispositivo", Toast.LENGTH_SHORT).show();
                    });
                } catch (SecurityException e) {
                    e.printStackTrace();
                    // Gestisci l'errore (mostra un messaggio all'utente)
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Errore di sicurezza", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        } else {
            Toast.makeText(getContext(), "Dispositivo non trovato", Toast.LENGTH_SHORT).show();
        }
    }

    private void disconnectFromDevice(BluetoothDeviceDto bluetoothDevice) {
        // Chiudi il socket
        BluetoothSocketHolder socketHolder = BluetoothSocketHolder.getInstance();
        BluetoothSocket socket = socketHolder.getSocket();

        try {
            if (socket != null && socket.isConnected()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Ferma il servizio
        stopBluetoothScannerService();

        // Resetta il BluetoothSocketHolder
        socketHolder.clear();

        // Aggiorna lo stato del dispositivo
        bluetoothDevice.setConnected(false);

        // Aggiorna l'UI sul thread principale
        requireActivity().runOnUiThread(() -> {
            // Notifica l'adapter dei cambiamenti
            pairedDevicesAdapter.notifyDataSetChanged();
            nearbyDevicesAdapter.notifyDataSetChanged();

            Toast.makeText(getContext(), "Disconnesso da " + bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void stopBluetoothScannerService() {
        Intent serviceIntent = new Intent(getContext(), BluetoothScannerService.class);
        requireContext().stopService(serviceIntent);
    }

    private void startBluetoothScannerService() {
        Intent serviceIntent = new Intent(getContext(), BluetoothScannerService.class);
        ContextCompat.startForegroundService(requireContext(), serviceIntent);
    }


    @Override
    public void onLongItemClick(BluetoothDeviceDto bluetoothDevice) {
        if (bluetoothDevice == null || bluetoothDevice.getAddress() == null) {
            Toast.makeText(getContext(), "Dispositivo non valido", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Elimina dispositivo")
                .setMessage("Vuoi davvero eliminare questo dispositivo dalla lista associati?")
                .setPositiveButton("Conferma", (dialog, which) -> {
                    removePairedDevice(bluetoothDevice);
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    // Metodo per rimuovere un dispositivo associato
    private void removePairedDevice(BluetoothDeviceDto bluetoothDevice) {
        // Rimuovi il dispositivo dal BluetoothAdapter se è associato
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress());
        if (device != null) {
            try {
                // Non c'è un metodo diretto per dissociare un dispositivo in BluetoothAdapter,
                // ma possiamo rimuoverlo dalla lista gestita dal ViewModel
                List<BluetoothDeviceDto> pairedDevices = bluetoothDeviceViewModel.getPairedBluetoothDevices().getValue();
                if (pairedDevices != null) {
                    pairedDevices.removeIf(d -> d.getAddress().equals(bluetoothDevice.getAddress()));
                    bluetoothDeviceViewModel.setPairedBluetoothDevices(pairedDevices);
                    Toast.makeText(getContext(), "Dispositivo rimosso", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Errore nella rimozione del dispositivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    private void fetchPairedDevices() {
        // Verifica se il Bluetooth è abilitato
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            enableBluetooth();
            return;
        }

        // Verifica il permesso BLUETOOTH_CONNECT
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permesso BLUETOOTH_CONNECT non concesso", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> pairedDevicesSet = bluetoothAdapter.getBondedDevices();

        List<BluetoothDeviceDto> pairedDevicesList = new ArrayList<>();

        if (pairedDevicesSet != null && !pairedDevicesSet.isEmpty()) {
            for (BluetoothDevice device : pairedDevicesSet) {
                BluetoothDeviceDto deviceDto = new BluetoothDeviceDto();
                deviceDto.setName(device.getName());
                deviceDto.setAddress(device.getAddress());
                deviceDto.setCreationDate(ZonedDateTime.now(ZoneId.of("Europe/Rome")));

                pairedDevicesList.add(deviceDto);
            }
        }

        bluetoothDeviceViewModel.setPairedBluetoothDevices(pairedDevicesList);
    }

    private boolean checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(),
                    permissionsNeeded.toArray(new String[0]),
                    ConstantUtils.REQUEST_BLUETOOTH_PERMISSION);
            return false;
        }

        return true;
    }

}