import java.util.*;
import java.util.stream.Collectors;

public class IronCrossSim {

    private static final Random rng = new Random();
    private static final Map<Integer, Integer> diceRolls = new HashMap<>();

    public static void main(String[] args) {
        int startingCash = 500;
        int winsUntilPress = 3;
        int iterations = 10000;
        List<SimResult> results = new ArrayList<>();
        for(int i = 0; i < iterations; i++) {
            results.add(runSim(startingCash, winsUntilPress));
        }

        //Lazy as shit
        int rollsP25 = results.stream()
                .map(r -> r.noRolls)
                .sorted()
                .collect(Collectors.toList()).get(Double.valueOf(0.25*iterations).intValue());

        int rollsP50 = results.stream()
                .map(r -> r.noRolls)
                .sorted()
                .collect(Collectors.toList()).get(Double.valueOf(0.50*iterations).intValue());

        int rollsP75 = results.stream()
                .map(r -> r.noRolls)
                .sorted()
                .collect(Collectors.toList()).get(Double.valueOf(0.75*iterations).intValue());

        int bankP25 = results.stream()
                .map(r -> r.highestBankroll)
                .sorted()
                .collect(Collectors.toList()).get(Double.valueOf(0.25*iterations).intValue());

        int bankP50 = results.stream()
                .map(r -> r.highestBankroll)
                .sorted()
                .collect(Collectors.toList()).get(Double.valueOf(0.50*iterations).intValue());

        int bankP75 = results.stream()
                .map(r -> r.highestBankroll)
                .sorted()
                .collect(Collectors.toList()).get(Double.valueOf(0.75*iterations).intValue());

        int placeP25 = results.stream()
                .map(r -> r.highestPlaceBet)
                .sorted()
                .collect(Collectors.toList()).get(Double.valueOf(0.25*iterations).intValue());

        int placeP50 = results.stream()
                .map(r -> r.highestPlaceBet)
                .sorted()
                .collect(Collectors.toList()).get(Double.valueOf(0.50*iterations).intValue());

        int placeP75 = results.stream()
                .map(r -> r.highestPlaceBet)
                .sorted()
                .collect(Collectors.toList()).get(Double.valueOf(0.75*iterations).intValue());

        IntSummaryStatistics rollStats = results.stream().map(r -> r.noRolls).mapToInt(Integer::intValue).summaryStatistics();
        IntSummaryStatistics bankStats = results.stream().map(r -> r.highestBankroll).mapToInt(Integer::intValue).summaryStatistics();
        IntSummaryStatistics placeStats = results.stream().map(r -> r.highestPlaceBet).mapToInt(Integer::intValue).summaryStatistics();

        System.out.println("Results for starting bankroll: " + startingCash + " and " + winsUntilPress + " hits until press, " + iterations + " iterations");
        System.out.println();

        System.out.println(String.format("Rolls min: %d max: %d avg: %f, p25: %d p50: %d p75: %d", rollStats.getMin(), rollStats.getMax(), rollStats.getAverage(), rollsP25, rollsP50, rollsP75));
        System.out.println(String.format("Max bankroll min: %d max: %d avg: %f, p25: %d p50: %d p75: %d", bankStats.getMin(), bankStats.getMax(), bankStats.getAverage(), bankP25, bankP50, bankP75));
        System.out.println(String.format("Max place bet min: %d max: %d avg: %f, p25: %d p50: %d p75: %d", placeStats.getMin(), placeStats.getMax(), placeStats.getAverage(), placeP25, placeP50, placeP75));

        System.out.println();
        System.out.println("Diceroll distribution: ");
        diceRolls.entrySet().stream().forEach(r -> System.out.println(r.getKey() + ": " + r.getValue()));
    }


    private static SimResult runSim(int startingCash, int winsUntilPress) {
        int rollsTillDone = 0;
        int rollsSincePress = 0;
        int startingFiveBet = 15;
        int startingSixBet = 18;
        int startingEightBet = 18;
        int startingFieldBet = 10;

        int bankroll = startingCash;
        int currentFiveBet = startingFiveBet;
        int currentSixBet = startingSixBet;
        int currentEightBet = startingEightBet;
        int currentFieldBet = startingFieldBet;

        int highestBankroll = bankroll;
        int highestPlacebet = startingEightBet;

        while (bankroll >= 61) {
            if(rollsSincePress >= winsUntilPress) {
                currentFiveBet += 5;
                currentSixBet += 6;
                currentEightBet += 6;
                currentFieldBet += 5;

                rollsSincePress = 0;
            }

            bankroll = bankroll - currentFiveBet - currentSixBet - currentEightBet -currentFieldBet;
            int result = runIronCross(currentFiveBet, currentSixBet, currentEightBet, currentFieldBet);
            bankroll += result;
            if(result == 0) {
                rollsSincePress = 0;
                currentFiveBet = startingFiveBet;
                currentSixBet = startingSixBet;
                currentEightBet = startingEightBet;
                currentFieldBet = startingFieldBet;
            } else {
                rollsSincePress += 1;
            }

            highestBankroll = (bankroll > highestBankroll) ? bankroll : highestBankroll;
            highestPlacebet = (currentEightBet > highestPlacebet) ? currentEightBet : highestPlacebet;
            rollsTillDone++;
        }

        SimResult res = new SimResult();
        res.highestBankroll = highestBankroll;
        res.highestPlaceBet = highestPlacebet;
        res.noRolls = rollsTillDone;

        return res;
    }

    private static int rollDice() {
        int dice1 = rng.nextInt(6) + 1;
        int dice2 = rng.nextInt(6) + 1;
        int roll = dice1 + dice2;

        Integer rollFromMap = diceRolls.get(roll);
        if(rollFromMap == null) {
            diceRolls.put(roll, 1);
        } else {
            diceRolls.put(roll, rollFromMap + 1);
        }

        return roll;
    }

    //Assumes double payout for hi/lo on field
    private static int runIronCross(int fiveBet, int sixBet, int eightBet, int fieldBet) {
        int initialBet = fieldBet + fiveBet + sixBet + eightBet;
        int diceRoll = rollDice();
        switch (diceRoll) {
            case 2:
                return initialBet + (2*fieldBet);
            case 3:
                return initialBet + fieldBet;
            case 4:
                return initialBet + fieldBet;
            case 5:
                return initialBet - fieldBet + new Double((6/5.0) * fiveBet).intValue();
            case 6:
                return initialBet - fieldBet + new Double((7/6.0) * sixBet).intValue();
            case 7:
                return 0;
            case 8:
                return initialBet - fieldBet + new Double((7/6.0) * eightBet).intValue();
            case 9:
                return initialBet + fieldBet;
            case 10:
                return initialBet + fieldBet;
            case 11:
                return initialBet + fieldBet;
            case 12:
                return initialBet + (2*fieldBet);

            default:
                return initialBet;
        }
    }


    static class SimResult {
        int noRolls;
        int highestPlaceBet;
        int highestBankroll;
    }
}
