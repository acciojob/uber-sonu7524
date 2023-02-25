package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		Customer customer = customerRepository2.findById(customerId).get();

//	   newTrip.setCustomer(customer);
		List<Driver>driverList=driverRepository2.findAll();
		Driver driverAvailable = null;
		for(Driver driver:driverList){
			if(driver.getCab().getAvailable() == true){
				driverAvailable = driver;
				break;
			}
		}
		if(driverAvailable==null){
			throw new Exception("No cab available!");
		}

		// set trip details
		int ratePerKm=driverAvailable.getCab().getPerKmRate();
		int totalBill=ratePerKm*distanceInKm;
		TripBooking newTrip=new TripBooking();
		newTrip.setFromLocation(fromLocation);
		newTrip.setToLocation(toLocation);
		newTrip.setDistanceInKm(distanceInKm);
		newTrip.setCustomer(customer);
		newTrip.setDriver(driverAvailable);
		newTrip.setBill(totalBill);
		newTrip.setStatus(TripStatus.CONFIRMED);

		//update bookinglist in customer data
		List<TripBooking>listOfTrip=customer.getTripBookingList();
		listOfTrip.add(newTrip);
		customer.setTripBookingList(listOfTrip);
		customerRepository2.save(customer);

		//update bookinglist in driver data
		List<TripBooking>tripBookingList=driverAvailable.getTripBookingList();
		tripBookingList.add(newTrip);
		driverAvailable.setTripBookingList(tripBookingList);
		driverRepository2.save(driverAvailable);

		return newTrip;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBookingRepository2.save(tripBooking);

		//update driver triplist
		Driver driver = tripBooking.getDriver();
		List<TripBooking> tripBookingList = driver.getTripBookingList();
		tripBookingList.add(tripBooking);
		driverRepository2.save(driver);

		//update customer triplist
		Customer customer = tripBooking.getCustomer();
		List<TripBooking> tripBookingList1 = customer.getTripBookingList();
		tripBookingList1.add(tripBooking);
		customerRepository2.save(customer);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBookingRepository2.save(tripBooking);

		//update driver triplist
		Driver driver = tripBooking.getDriver();
		List<TripBooking> tripBookingList = driver.getTripBookingList();
		tripBookingList.add(tripBooking);
		driverRepository2.save(driver);

		//update customer triplist
		Customer customer = tripBooking.getCustomer();
		List<TripBooking> tripBookingList1 = customer.getTripBookingList();
		tripBookingList1.add(tripBooking);
		customerRepository2.save(customer);

	}

}
