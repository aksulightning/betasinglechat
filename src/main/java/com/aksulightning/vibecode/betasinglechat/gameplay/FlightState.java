package com.aksulightning.vibecode.betasinglechat.gameplay;

public interface FlightState {
    boolean betasinglechat$isFlying();

    void betasinglechat$setFlying(boolean enabled);

    double betasinglechat$getFlightSpeed();

    void betasinglechat$setFlightSpeed(double speed);

    void betasinglechat$resetFallDistance();
}
