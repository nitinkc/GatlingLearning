# Lab 2: HTTP with Feeders
## Learning Objectives
- ✅ Load data from CSV files (feeders)
- ✅ Understand feeder strategies (circular, random, queue)
- ✅ Variable substitution with #{variable}
- ✅ Making realistic multi-user scenarios
## Real-World Scenario
Test a user API where each simulated user should be different. Using a CSV file with real user IDs ensures realistic testing.
## Quick Summary
```bash
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim02_HttpWithFeeders
```
See full lab walkthrough by opening the simulation code in your IDE.
## Success Criteria
✅ Different users have different userIds (from CSV)  
✅ >200 requests total  
✅ Success rate >99%  
✅ Each request uses different data  
## Next Steps
→ **Lab 3**: [Lab 3: Checks & Validation](03-lab-checks-validation.md)
