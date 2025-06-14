package ru.walkAndTalk.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale

class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() = callbackFlow {
        // Лог начала получения местоположения
        Log.d("LocationService", "getCurrentLocation: Начало получения местоположения")

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // Лог получения координат
                    Log.d("LocationService", "getCurrentLocation: Получены координаты - Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                    trySend(LocationData(latitude = location.latitude, longitude = location.longitude))
                } ?: run {
                    // Лог отсутствия местоположения
                    Log.w("LocationService", "getCurrentLocation: Местоположение не получено")
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            // Лог завершения получения местоположения
            Log.d("LocationService", "getCurrentLocation: Завершение получения местоположения")
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    fun getCityFromCoordinates(latitude: Double, longitude: Double): String? {
        // Лог начала геокодирования
        Log.d("LocationService", "getCityFromCoordinates: Начало преобразования координат - Latitude: $latitude, Longitude: $longitude")
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val city = addresses?.firstOrNull()?.locality
            // Лог успешного получения города
            if (city != null) {
                Log.d("LocationService", "getCityFromCoordinates: Город определён - $city")
            } else {
                Log.w("LocationService", "getCityFromCoordinates: Город не определён")
            }
            city
        } catch (e: Exception) {
            // Лог ошибки геокодирования
            Log.e("LocationService", "getCityFromCoordinates: Ошибка преобразования координат: ${e.message}")
            null
        }
    }
}
// class LocationService(private val context: Context) {
//    private val fusedLocationClient: FusedLocationProviderClient =
//        LocationServices.getFusedLocationProviderClient(context)
//
//    @SuppressLint("MissingPermission")
//    fun getCurrentLocation() = callbackFlow {
//        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
//            .setMinUpdateIntervalMillis(5000)
//            .build()
//
//        val locationCallback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                result.lastLocation?.let { location ->
//                    trySend(LocationData(latitude = location.latitude, longitude = location.longitude))
//                }
//            }
//        }
//
//        fusedLocationClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            Looper.getMainLooper()
//        )
//
//        awaitClose {
//            fusedLocationClient.removeLocationUpdates(locationCallback)
//        }
//    }
//
//    fun getCityFromCoordinates(latitude: Double, longitude: Double): String? {
//        return try {
//            val geocoder = Geocoder(context, Locale.getDefault())
//            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//            addresses?.firstOrNull()?.locality
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//}

data class LocationData(val latitude: Double, val longitude: Double)