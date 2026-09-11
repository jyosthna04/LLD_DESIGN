package com.designloop.service;

import com.designloop.model.Problem;
import com.designloop.repository.ProblemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Runs once every time the app starts. If the "problems" table is
 * already empty, it inserts the 3 starter LLD problems. If problems
 * already exist (from a previous run), it does nothing - this is what
 * stops duplicate rows from appearing every time you restart the app.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final ProblemRepository problemRepository;

    public DataSeeder(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Override
    public void run(String... args) {
        if (problemRepository.count() > 0) {
            return;
        }

        Problem parkingLot = new Problem();
        parkingLot.setTitle("Parking Lot");
        parkingLot.setDifficulty("Easy");
        parkingLot.setDescription("Design a parking lot system that can park vehicles of different sizes, issue tickets, and calculate parking fees.");
        parkingLot.setRequirements("- Support multiple vehicle types (car, bike, truck)\n- Support multiple parking slot sizes\n- Issue a ticket when a vehicle enters\n- Calculate fee when a vehicle exits, based on duration\n- Track available slots");
        parkingLot.setRequiredConcepts("Vehicle,ParkingLot,Slot,Ticket");
        problemRepository.save(parkingLot);

        Problem elevator = new Problem();
        elevator.setTitle("Elevator System");
        elevator.setDifficulty("Medium");
        elevator.setDescription("Design an elevator control system for a building with multiple elevators and floors.");
        elevator.setRequirements("- Support multiple elevators\n- Handle external requests (floor + direction) and internal requests (destination floor)\n- Decide which elevator should respond to a request\n- Track elevator state (idle, moving up, moving down, door open)\n- Handle simultaneous requests");
        elevator.setRequiredConcepts("Elevator,Floor,Request,Controller");
        problemRepository.save(elevator);

        Problem vendingMachine = new Problem();
        vendingMachine.setTitle("Vending Machine");
        vendingMachine.setDifficulty("Easy");
        vendingMachine.setDescription("Design a vending machine that accepts money, lets a user select a product, dispenses it, and returns change.");
        vendingMachine.setRequirements("- Support multiple products with prices and stock counts\n- Accept coins/notes\n- Select a product\n- Dispense product if enough money was inserted and it's in stock\n- Return change\n- Handle out-of-stock and insufficient-money cases");
        vendingMachine.setRequiredConcepts("Product,Inventory,VendingMachine,Payment");
        problemRepository.save(vendingMachine);
    }
}
