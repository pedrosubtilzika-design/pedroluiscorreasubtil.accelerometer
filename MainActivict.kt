```kotlin
package br.edu.pedroluiscorreasubtil.accelerometerapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import br.edu.pedroluiscorreasubtil.accelerometerapp.ui.theme.AccelerometerAppTheme
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccelerometerAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AccelerometerScreen()
                }
            }
        }
    }
}

/*
 * ViewModel responsável por armazenar os valores lidos
 * pelo sensor e disponibilizá-los para a interface Compose.
 */
class AccelerometerViewModel : ViewModel(), SensorEventListener {

    var eixoX by mutableStateOf(0f)
        private set

    var eixoY by mutableStateOf(0f)
        private set

    var eixoZ by mutableStateOf(0f)
        private set

    var aceleracaoLinear by mutableStateOf(0f)
        private set

    var gravidade by mutableStateOf(0f)
        private set

    var sensorDisponivel by mutableStateOf(true)
        private set

    private var sensorManager: SensorManager? = null
    private var acelerometro: Sensor? = null
    private var sensorGravidade: Sensor? = null

    /*
     * Inicializa os sensores do aparelho.
     */
    fun iniciarSensores(context: Context) {

        sensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        acelerometro =
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorGravidade =
            sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)

        sensorDisponivel = acelerometro != null

        acelerometro?.let {
            sensorManager?.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        sensorGravidade?.let {
            sensorManager?.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {

        event ?: return

        when (event.sensor.type) {

            Sensor.TYPE_ACCELEROMETER -> {

                eixoX = event.values[0]
                eixoY = event.values[1]
                eixoZ = event.values[2]

                /*
                 * A aceleração total é calculada a partir
                 * dos três eixos utilizando o módulo do vetor.
                 */
                val moduloTotal = sqrt(
                    eixoX * eixoX +
                            eixoY * eixoY +
                            eixoZ * eixoZ
                )

                /*
                 * Aproximação da aceleração linear:
                 * remove aproximadamente a gravidade de 9,81 m/s².
                 */
                aceleracaoLinear =
                    kotlin.math.abs(moduloTotal - 9.81f)
            }

            Sensor.TYPE_GRAVITY -> {

                /*
                 * O sensor de gravidade fornece diretamente
                 * a aceleração causada pela gravidade.
                 */
                gravidade = sqrt(
                    event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2]
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Não é necessário tratar a precisão para esta atividade.
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccelerometerScreen(
    viewModel: AccelerometerViewModel = viewModel()
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    /*
     * Inicia os sensores quando a tela é criada.
     */
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.iniciarSensores(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Leitura do Acelerômetro",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pedro Luís Correa Subtil",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Monitoramento em tempo real",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (!viewModel.sensorDisponivel) {

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Este dispositivo não possui um acelerômetro disponível.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }

            } else {

                Text(
                    text = "Aceleração nos eixos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    SensorCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Eixo X",
                        valor = viewModel.eixoX
                    )

                    SensorCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Eixo Y",
                        valor = viewModel.eixoY
                    )

                    SensorCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Eixo Z",
                        valor = viewModel.eixoZ
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Informações adicionais",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                InformationCard(
                    titulo = "Aceleração linear",
                    valor = viewModel.aceleracaoLinear
                )

                InformationCard(
                    titulo = "Aceleração da gravidade",
                    valor = viewModel.gravidade
                )

                Spacer(modifier = Modifier.height(8.dp))

                /*
                 * Indicador visual para informar ao usuário
                 * que os dados estão sendo atualizados.
                 */
                Text(
                    text = "● Sensor ativo • Atualização em tempo real",
                    style = MaterialTheme.typography.bodyMedium
                )

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SensorCard(
    modifier: Modifier = Modifier,
    titulo: String,
    valor: Float
) {

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "%.2f".format(valor),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "m/s²",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun InformationCard(
    titulo: String,
    valor: Float
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = "%.2f".format(valor),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "m/s²",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
```
