package jvn;

public enum LockState {
    NL, // no lock
    R, // read lock taken
    W, // write lock taken
    RC, // read lock cached
    WC, // write lock cached
    RWC; // write lock cached & read lock taken
}
