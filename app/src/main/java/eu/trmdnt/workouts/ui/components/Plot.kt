package eu.trmdnt.workouts.ui.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import eu.trmdnt.workouts.database.entities.statistics.WeightOnDate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

data class DataPoint(
    val xPosition: Float, val date: Date, val value1: Float, val value2: Float
)

val dateFormat = SimpleDateFormat("yyyy-MM-dd")
val dayMonthFormat = SimpleDateFormat("dd.MM")

@Composable
fun DateHistoryPlot(
    data: List<DataPoint>,
    yAxis1: String,
    yAxis2: String,
    selectedPoint: Int?,
    onPointSelected: (Int) -> Unit
) {
    if (data.isEmpty()) return

    val TAG = "DateHistoryPlot"

    val lineColor = MaterialTheme.colorScheme.onBackground
    val dataOneColor = MaterialTheme.colorScheme.tertiary
    val dataTwoColor = MaterialTheme.colorScheme.primary
    val lineWidth = 4f
    val borderOffset = 80f
    val axisPointSize = 15f
    val textStyle = MaterialTheme.typography.bodyLarge
    val textStyleSmall = MaterialTheme.typography.bodySmall

    val textMeasurer = rememberTextMeasurer()
    val labelYAxis1 = yAxis1
    val labelYAxis2 = yAxis2

    val data1Max = remember(data) { data.maxOf { it.value1 } }
    val data2Max = remember(data) { data.maxOf { it.value2 } }

    val data1Min = data.minOf { it.value1 }
    val data2Min = data.minOf { it.value2 }

    val plot1Zero = 0f
    val plot1Max = ceil(data1Max)
    val plot2Zero = 0f
    val plot2Max = ceil(data2Max)

    //used to store the position of a click in the canvas
    var pressedPosition by remember { mutableStateOf<Float?>(null) }

    Box(Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(key1 = Unit) {
                    detectTapGestures(
                        onPress = {
                            pressedPosition = it.x
                        }
                    )
                }
        ) {
            val height = size.height
            val width = size.width

            val topLeft = Offset(borderOffset, borderOffset)
            val topRight = Offset(width - borderOffset, borderOffset)
            val bottomLeft = Offset(borderOffset, height - borderOffset)
            val bottomRight = Offset(width - borderOffset, height - borderOffset)

            val xDrawSize = bottomRight.x - bottomLeft.x
            val yDrawSize = topLeft.y - bottomLeft.y

            fun getDrawingZoneOffset(x: Float, y: Float): Offset {
                return Offset(bottomLeft.x + (x * xDrawSize), bottomLeft.y + (y * yDrawSize))
            }

            fun realXCoordinateToDrawingZone(x: Float): Float {
                return (x - bottomLeft.x) / xDrawSize
            }

            pressedPosition?.let { it ->
                pressedPosition = null
                val xCoord = realXCoordinateToDrawingZone(it)

                val index = data.withIndex().minBy { it ->
                    abs(xCoord - it.value.xPosition)
                }.index

                onPointSelected(index)
            }

            selectedPoint?.let {
                drawLine(
                    color = lineColor,
                    start = getDrawingZoneOffset(data[it].xPosition, 0f),
                    end = getDrawingZoneOffset(data[it].xPosition, 1f),
                    strokeWidth = lineWidth,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(10f, 10f),
                        phase = 0f
                    )
                )
            }

            fun getLabelPositions(
                val1: Float,
                val2: Float,
                averageLabelPoints: Int = 10
            ): List<Float> {
                Log.d(TAG, "getLabelPositions: $val1, $val2, $averageLabelPoints")
                val min = min(val1, val2)
                val max = max(min, val2)
                val valueRange = max - min

                val diffBetweenPoints =
                    max((valueRange.toInt() / averageLabelPoints).toFloat(), 1.0f)

                return buildList {
                    if (0 >= min && 0 <= max) {
                        var currentValue = 0.0f
                        while (currentValue < max) {
                            add(currentValue)
                            currentValue = currentValue + diffBetweenPoints
                        }
                        currentValue = -diffBetweenPoints
                        while (currentValue > min) {
                            add(currentValue)
                            currentValue = currentValue - diffBetweenPoints
                        }
                        add(min)
                        add(max)
                    } else {
                        var currentValue = min
                        while (currentValue < max) {
                            add(currentValue)
                            currentValue = currentValue + diffBetweenPoints
                        }
                        add(max)

                    }
                    distinct()
                }
            }


            //draw axis 1
            run {
                drawLine(
                    color = lineColor, start = topLeft, end = bottomLeft, strokeWidth = lineWidth
                )
                val measuredYLabel = textMeasurer.measure(
                    text = labelYAxis1,
                    style = textStyle.copy(color = dataOneColor),
                )
                val yLabelTopLeft =
                    topLeft.copy(
                        x = 0f,
                        y = topLeft.y - measuredYLabel.size.height - 20f
                    )
                drawText(measuredYLabel, topLeft = yLabelTopLeft)

                //TODO half duplicated code
                //labels
                getLabelPositions(plot1Zero, plot1Max).forEach {
                    val range = plot1Max - plot1Zero
                    val labelOffset =
                        getDrawingZoneOffset(0f, (it - plot1Zero) / (plot1Max - plot1Zero))
                    drawLine(
                        color = lineColor,
                        start = labelOffset,
                        end = labelOffset.copy(x = labelOffset.x - axisPointSize),
                        strokeWidth = lineWidth
                    )
                    val measuredLabel = textMeasurer.measure(
                        //TODO questionable way to display number instead of number.0
                        text = if (ceil(it) == it) it.toInt().toString() else it.toString(),
                        style = textStyle.copy(color = dataOneColor),
                    )

                    val textHeight = measuredLabel.size.height
                    val textWidth = measuredLabel.size.width
                    drawText(
                        measuredLabel,
                        topLeft = Offset(
                            labelOffset.x - textWidth - 5f - axisPointSize,
                            labelOffset.y - textHeight / 2
                        ),
                    )
                }

            }

            //draw axis 2
            run {
                drawLine(
                    color = lineColor, start = topRight, end = bottomRight, strokeWidth = lineWidth
                )
                val measuredYLabel = textMeasurer.measure(
                    text = labelYAxis2,
                    style = textStyle.copy(color = dataTwoColor),
                )
                val overdraw = topRight.x + measuredYLabel.size.width - width
                val yLabelTopRight = topLeft.copy(
                    x = topRight.x - overdraw, y = topRight.y - measuredYLabel.size.height - 20f
                )
                drawText(measuredYLabel, topLeft = yLabelTopRight)

                getLabelPositions(plot2Zero, plot2Max).forEach {
                    val labelOffset =
                        getDrawingZoneOffset(1f, (it - plot2Zero) / (plot2Max - plot2Zero))
                    drawLine(
                        color = lineColor,
                        start = labelOffset,
                        end = labelOffset.copy(x = labelOffset.x + axisPointSize),
                        strokeWidth = lineWidth
                    )
                    val measuredLabel = textMeasurer.measure(
                        text = if (ceil(it) == it) it.toInt().toString() else it.toString(),
                        style = textStyle.copy(color = dataTwoColor),
                    )

                    val textHeight = measuredLabel.size.height
                    val textWidth = measuredLabel.size.width
                    drawText(
                        measuredLabel,
                        topLeft = Offset(
                            labelOffset.x + axisPointSize + 5f,
                            labelOffset.y - textHeight / 2
                        ),
                    )
                }
            }

            //draw x-Axis
            drawLine(
                color = lineColor, start = bottomLeft, end = bottomRight, strokeWidth = lineWidth
            )

            val data1Path = Path()
            data1Path.moveTo(bottomLeft.x, bottomLeft.y)
            val data1Points = mutableListOf<Offset>()
            val data2Path = Path()
            data2Path.moveTo(bottomLeft.x, bottomLeft.y)
            val data2Points = mutableListOf<Offset>()

            //draw points
            data.forEach { dataPoint ->
                val offset = getDrawingZoneOffset(dataPoint.xPosition, 0f)
                drawLine(
                    color = lineColor,
                    start = getDrawingZoneOffset(dataPoint.xPosition, 0f),
                    end = offset.copy(y = offset.y + axisPointSize),
                    strokeWidth = lineWidth
                )

                val data1Point =
                    getDrawingZoneOffset(
                        dataPoint.xPosition,
                        (dataPoint.value1 - plot1Zero) / (plot1Max - plot1Zero)
                    )
                data1Points.add(data1Point)
                data1Path.lineTo(data1Point.x, data1Point.y)

                val data2Point =
                    getDrawingZoneOffset(
                        dataPoint.xPosition,
                        (dataPoint.value2 - plot2Zero) / (plot2Max - plot2Zero)
                    )
                data2Points.add(data2Point)
                data2Path.lineTo(data2Point.x, data2Point.y)
            }

            data2Path.lineTo(bottomRight.x, bottomRight.y)
            data1Path.lineTo(bottomRight.x, bottomRight.y)

            data2Path.lineTo(bottomLeft.x, bottomLeft.y)
            data1Path.lineTo(bottomLeft.x, bottomLeft.y)

            data1Path.close()
            data2Path.close()

            drawPath(data1Path, color = dataOneColor.copy(alpha = 0.5f))
            drawPoints(
                points = data1Points,
                color = dataOneColor,
                strokeWidth = lineWidth * 5,
                pointMode = PointMode.Points,
                cap = StrokeCap.Round
            )

            drawPoints(
                points = data1Points,
                color = dataOneColor,
                strokeWidth = lineWidth,
                pointMode = PointMode.Polygon,
                cap = StrokeCap.Round
            )

            drawPath(data2Path, color = dataTwoColor.copy(alpha = 0.5f))
            drawPoints(
                points = data2Points,
                color = dataTwoColor,
                pointMode = PointMode.Points,
                strokeWidth = lineWidth * 5,
                cap = StrokeCap.Round,
            )
            drawPoints(
                points = data2Points,
                color = dataTwoColor,
                pointMode = PointMode.Polygon,
                strokeWidth = lineWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

fun getDaysSince(date: Date, since: Date): Long {
    return (date.toInstant().epochSecond - since.toInstant().epochSecond) / (3600 * 24)
}

@Composable
fun WeightRepHistoryPlot(
    data: List<WeightOnDate>,
    pointSelected: Int?,
    onPointSelected: (Int) -> Unit
) {
    Log.d("TAG", "WeightRepHistoryPlot: $data")

    if (!data.isEmpty()) {
        val startDate = dateFormat.parse(data.first().date)!!
        val endDate = dateFormat.parse(data.last().date)!!

        val totalDays = getDaysSince(endDate, startDate)

        val dataPoints = data.mapIndexed { i, value ->
            val date = dateFormat.parse(value.date)!!
            DataPoint(
                date = date, xPosition = getDaysSince(date, startDate).toFloat() / totalDays,
                //TODO fix data types to float
                value1 = value.weightPerRep.toFloat(), value2 = value.totalReps.toFloat()
            )
        }

        DateHistoryPlot(dataPoints, "weight/reps", "totalReps", pointSelected) {
            onPointSelected(it)
        }
    } else {
        Text("empty")
    }
}

@Preview
@Composable
fun WeightHistoryPlotPreview() {
    val data: List<WeightOnDate> = listOf(
        WeightOnDate(
            date = "2025-06-25",
            totalWeight = 300.0,
            totalReps = 30.0,
            weightPerRep = 5.0,
            dateTimeStamp = 1750870221
        ),

        WeightOnDate(
            date = "2025-06-29",
            totalWeight = 350.0,
            totalReps = 35.0,
            weightPerRep = 10.0,
            dateTimeStamp = 1751215821
        ),

        WeightOnDate(
            date = "2025-07-15",
            totalWeight = 250.0,
            totalReps = 30.0,
            weightPerRep = 15.0,
            dateTimeStamp = 1752598221
        ),

        WeightOnDate(
            date = "2025-07-29",
            totalWeight = 250.0,
            totalReps = 15.0,
            weightPerRep = 25.0,
            dateTimeStamp = 1752598221
        ),
    )

    WeightRepHistoryPlot(data, 2, {})
}