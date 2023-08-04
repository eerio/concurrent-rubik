# Concurrent Rubik's Cube Simulator

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Scramble.svg/1200px-Scramble.svg.png" alt="[Image of Rubik's cube]" width=200/>

This repository contains a concurrent Rubik's Cube simulator. This project was developed as part of the Concurrent Programming course at the University of Warsaw (MIMUW). The synchronization algorithm allows the non-colliding sides to rotate in parallel. The algorithm is *not* easy - for its description (in Polish), see problem 2: "Synchronizacja grupowa" ("Group synchronization") in the file `03-semafory.pdf`.

What do I personally like about this implementation?
- the testing was fun. I didn't really want to test rotations of NxN cube by hand, so I used the fact that after repeating a sequence of moves, you will always eventually go back to the original state. The longest such sequence is 1260 moves long (source: Wikipedia, proof by group theory) and thus I used it (and many other sequences with known periods) to test that my rotation implementation is correct.
- the synchronization algorithm is fairly complex and very delicate (as always with synchronization algorithms). It uses a technique called in Polish ,,dziedziczenie sekcji krytycznej'' (critical section inheritance?), which I believe is not widely known nor used outside of MIMUW. My friends and I tried many times to find English information on this technique, without success. It's idea is that a thread which pauses at some mutex A leaves a mutex B locked. Another thread wakes up and have it guaranteed that the lock B will be locked already, so it changes variables etc., and then releases the lock. When the original thread wakes up, it has it guaranteed that someone else has already released the lock it locked previously.

## Contributing
Please don't. I have already finished this course :)

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

We hope this simulator helps you in understanding the Rubik's cube and its inner workings. Feel free to explore the code, experiment with the interpreter, and adapt it for your specific needs. If you have any questions or need further assistance, don't hesitate to reach out.

Happy coding! 😊

