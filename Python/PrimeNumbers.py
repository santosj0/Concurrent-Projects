import sys
import threading
import time

"""
Purpose: This program was create to mirror the assignment assigned by the 'Concurrent Programming: Theory and Practice'
course at Rowan University. When executing this program in the command line, it requires a single positive number
argument that is greater than 0 and less than 2147483647. All other arguments will be ignored. In the menu, you will get
to choose between a single thread method and a multiple thread method for obtaining every prime number within the range 
of the passed argument. From there, you can examine the runtime difference between the two methods.

Author: James Santos
Last Modified: September 23, 2019
"""


class PossiblePrime(threading.Thread):
    def __init__(self, number, prime_list=[]):
        """
        Class for determining if the number is prime.
        :param number: Number for determining whether or not it is a prime number.
        :param prime_list: List to add to if the number is a prime
        """
        threading.Thread.__init__(self)
        self.num = number
        self.result = prime_list
        self.setName("Thread" + str(number))

    def run(self):
        # Adds to list if prime
        if determine_prime_number(self.num):
            self.result.append(self.num)


def prime_in_range(length):
    """
    Provides all of the prime numbers within the desired range.
    :param length: Range to get all prime numbers from.
    :return: A list of prime numbers from the desired range.
    """
    primes = []
    for i in range(1, length + 1):
        if determine_prime_number(i):
            primes.append(i)

    return primes


def determine_prime_number(possible_prime):
    """
    Determines if the number passed is a prime number
    :param possible_prime: Number for determining if prime.
    :return: Boolean if number is prime.
    """
    for i in range(2, int(possible_prime/2 + 1)):
        if possible_prime % i == 0:
            return False

    return True


# Menu Determination
def case1():
    """
    Single thread.
    :return: None.
    """
    global num_arg
    print("Computing Primes through a single Thread...")

    # Determine all prime numbers in range of argument as well as determining how long it took
    start_single = time.time()
    answers_single = prime_in_range(num_arg)
    runtime_single = time.time() - start_single

    # Output the results
    print("Primes are ", end="")
    print(*answers_single, sep=", ")
    print("That took", runtime_single, "seconds.")
    print("There are a total of", len(answers_single), "prime numbers.")


def case2():
    """
    Multi thread.
    :return: None.
    """
    global num_arg
    print("Computing Primes through multiple Threads...")

    threads = []                    # List of generated threads
    start_multi = time.time()       # Timer
    answers_multi = []              # List of prime numbers in the range of the argument

    # Thread creation
    for i in range(1, num_arg + 1):
        threads.append(PossiblePrime(i, answers_multi))
        threads[i - 1].start()

    # Make main thread wait until all previous threads have completed their tasks
    for thread in threads:
        thread.join()

    # Timer finishes
    runtime_multi = time.time() - start_multi

    # Output the results
    print("Primes are ", end="")
    print(*answers_multi, sep=", ")
    print("That took", runtime_multi, "seconds.")
    print("There are a total of", len(answers_multi), "prime numbers.")


def case3():
    """
    Modifies num_arg to check prime numbers in an alternate range.
    :return: None.
    """
    global num_arg
    new_num = input("Please input new number to test:\n")

    try:
        num_arg = int(new_num)
        assert 2147483647 > num_arg > 0, ""
    except (ValueError, AssertionError):
        print("Positive integers only that are less than 2147483647.")


def case4():
    """
    Display the current range for prime determination.
    :return: None.
    """
    print(num_arg, "is the number.")


def case5():
    """
    Exits out of the program.
    :return: None.
    """
    print("Thank you for using PrimeNumbers Python Edition.")
    exit()


def menu(choice):
    """
    Executes the choice made by the user.
    :param choice: Decision from the user.
    :return: Executes the selected case.
    """
    menu_items = {
        1: case1,
        2: case2,
        3: case3,
        4: case4,
        5: case5
    }

    return menu_items.get(choice)()


if __name__ == "__main__":
    # Grabs the parameter passed when script executes. Otherwise, stops the program immediately
    try:
        num_arg = int(sys.argv[1])
        assert 2147483647 > num_arg > 0, ""
    except (ValueError, IndexError, AssertionError):
        print("First argumment passed must be a positie number greater than 0 and less than 2147483647. All extra "
              "parameters will be ignored.")
        sys.exit()

    # Main program running
    while True:
        # User selects menu item
        decision = input("Please choose an option"
                         "\n1) Single threaded"
                         "\n2) Multi threaded"
                         "\n3) Change parameter"
                         "\n4) Display number"
                         "\n5) Exit"
                         "\n")

        # Make sure user passes correct input
        try:
            decision = int(decision)

            assert 0 < decision <= 5, ""

        except (ValueError, AssertionError):
            print("Please provide a number corrolating to the choices.\n")
            continue

        # Executes the user's decision
        menu(decision)
        print()
