package com.example.tanmay.rentbaazvehicleadministration.Entity.ToRent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.DatePicker
import com.bumptech.glide.Glide
import com.example.tanmay.rentbaazvehicleadministration.Entity.Home.VehicleModel
import com.example.tanmay.rentbaazvehicleadministration.Entity.Home.bookingModel
import com.example.tanmay.rentbaazvehicleadministration.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_to_rent.*
import java.text.SimpleDateFormat
import java.util.*


class ToRentActivity : AppCompatActivity() {

    lateinit var itemId: String
    var rootRef: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var pickUpDateTime: Date
    lateinit var returnDateTime: Date
    var vehicle: VehicleModel? = null
    lateinit var vehicleUpdated: VehicleModel
    var cal = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_rent)
        supportActionBar!!.title = Html.fromHtml("<font color=\"#a9a9a9\">Rent Out</font>")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //TODO: Change color of up enabled button
        //TODO: Disable Keyboard Popping

        val pickUpDateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd/MM/yyyy" // mention the format you need
                val simpleDateFormat = SimpleDateFormat(myFormat, Locale.US)
                edit_text_pick_up_date!!.text = Editable.Factory.getInstance().newEditable(simpleDateFormat.format(cal.getTime()))
                pickUpDateTime = cal.time
            }
        }

        val returnDateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd/MM/yyyy" // mention the format you need
                val simpleDateFormat = SimpleDateFormat(myFormat, Locale.US)
                edit_text_return_date!!.text = Editable.Factory.getInstance().newEditable(simpleDateFormat.format(cal.getTime()))
                returnDateTime = cal.time
            }
        }

        edit_text_pick_up_date.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        DatePickerDialog(this@ToRentActivity,
                                pickUpDateSetListener,
                                // se DatePickerDialog to point to today's date when it loads up
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)).show()
                    }
                }

                return v?.onTouchEvent(event) ?: true
            }
        })

        edit_text_return_date.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        DatePickerDialog(this@ToRentActivity,
                                returnDateSetListener,
                                // set DatePickerDialog to point to today's date when it loads up
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)).show()
                    }
                }

                return v?.onTouchEvent(event) ?: true
            }
        })

        val pickUpTimeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

            edit_text_pick_up_time.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("HH:mm").format(cal.time))
            pickUpDateTime = cal.time
        }

        val returnTimeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

            edit_text_return_time.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("HH:mm").format(cal.time))
            returnDateTime = cal.time
        }

        edit_text_pick_up_time.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        TimePickerDialog(this@ToRentActivity, pickUpTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })

        edit_text_return_time.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        TimePickerDialog(this@ToRentActivity, returnTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                    }
                }

                return v?.onTouchEvent(event) ?: true
            }
        })

    }

    override fun onStart() {
        super.onStart()
        itemId = intent.getStringExtra("item_id")
        Log.v("Item ID", itemId)
        val vehicleDocumentReference = FirebaseFirestore.getInstance().collection("vehicle").document(itemId)
        vehicleDocumentReference.get().addOnSuccessListener { it: DocumentSnapshot ->

            vehicle = it.toObject(VehicleModel::class.java)

            bike_name.text = it.get("vehicle_name").toString()
            Glide.with(this).load(it.get("vehicle_image_url")).into(bike_img)
            bike_number.text = it.get("vehicle_number").toString()
            bike_organization.text = it.get("vendor_organization").toString()
            cost_weekday.text = it.get("weekday_cost").toString()
            cost_weekend.text = it.get("weekend_cost").toString()
        }

        submit.setOnClickListener {
            vehicle?.booking?.add(bookingModel(number.text.toString(), Date(pickUpDateTime.time), Date(returnDateTime.time)))
            rootRef.collection("vehicle").document(itemId).set(vehicle!!)

            rootRef.collection("rentee_details").document(number.text.toString()).set(RenteeModel(first_name.text.toString(),
                    last_name.text.toString(),
                    number.text.toString(),
                    registration_number.text.toString()))
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
