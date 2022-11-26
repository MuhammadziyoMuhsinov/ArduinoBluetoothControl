package uz.muhammadziyo.bluetoothhc_6

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import uz.muhammadziyo.bluetoothhc_6.adapter.RvAdapter
import uz.muhammadziyo.bluetoothhc_6.adapter.rvClick
import uz.muhammadziyo.bluetoothhc_6.databinding.ActivityMainBinding
import uz.muhammadziyo.bluetoothhc_6.databinding.ItemDialogBinding
import uz.muhammadziyo.bluetoothhc_6.models.Bluetooth
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var btAdapter:BluetoothAdapter? = null
    private var btSocket: BluetoothSocket? = null
    //var address: String? = null
    private val BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var activar = false
    var bluetoothIn: Handler? = null
    val handlerState = 0
    private lateinit var list:ArrayList<Bluetooth>

    private var MyConexionBT: ConnectedThread? = null
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        list = ArrayList()

        verificarBluetooth()


                val pairedDeveicesList = btAdapter?.bondedDevices

                pairedDeveicesList?.forEach {
                    list.add(Bluetooth(it.name,it.address))
                }


        binding.connect.setOnClickListener {
            activar = true
            val alertDialog = AlertDialog.Builder(this).create()
            val itemDialog = ItemDialogBinding.inflate(layoutInflater)
            alertDialog.setView(itemDialog.root)
            alertDialog.show()
            itemDialog.rv.adapter = RvAdapter(list,object : rvClick{
                override fun OnClick(bluetooth: Bluetooth) {
                    OnResume(bluetooth)
                    alertDialog.cancel()
                }
            })

        }


        binding.btnSend.setOnClickListener {
            if (binding.connect.text.toString() == "Connect"){
                Snackbar.make(this,it,"device disconnected !",1000).show()
            }else {

                if (binding.edtMessage.text.toString().trim() == ""){
                    Toast.makeText(this, "Empty", Toast.LENGTH_SHORT).show()
                }else{
                    MyConexionBT!!.write(binding.edtMessage.text.toString())
                    binding.edtMessage.text.clear()
                }

            }
        }

        binding.unconnect.setOnClickListener {
            if (binding.connect.text.toString() == "Connect"){
                Snackbar.make(this,it,"device disconnected !",1000).show()
            }else {
                try {
                    btSocket!!.close()
                    binding.connect.text = "Connect"
                    Toast.makeText(this, "disconnected", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }



    }


    @SuppressLint("MissingPermission")
    private fun verificarBluetooth() {
        if (btAdapter!!.isEnabled) {
        } else {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, 1)
        }
    }


    @SuppressLint("MissingPermission")
    fun OnResume(bluetooth:Bluetooth) {
        if (activar) {
            val device: BluetoothDevice = btAdapter!!.getRemoteDevice(bluetooth.address)
            try {
                btSocket = createBluetoothSocket(device)
            } catch (e: IOException) {
                Toast.makeText(baseContext, "La creacción del Socket fallo", Toast.LENGTH_LONG)
                    .show()
            }
            // Establece la conexión con el socket Bluetooth.
            try {
                btSocket!!.connect()
                Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show()
                binding.connect.text = bluetooth.name
            } catch (e: IOException) {
                try {
                    btSocket!!.close()
                } catch (e2: IOException) {
                }
            }
            MyConexionBT = ConnectedThread(btSocket!!)
            MyConexionBT!!.start()
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket? {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID)
    }

    inner class ConnectedThread(socket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            val buffer = ByteArray(256)
            var bytes: Int

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream!!.read(buffer)
                    val readMessage = String(buffer, 0, bytes)
                    bluetoothIn!!.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget()
                } catch (e: IOException) {
                    break
                }
            }
        }

        //Envio de trama
        fun write(input: String) {
            try {
                mmOutStream!!.write(input.toByteArray())
            } catch (e: IOException) {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }
    }


}