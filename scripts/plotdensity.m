A = csvread("octave.csv");
x = A(:,1);
x = x/20;
y = A(:,2);
sy = A(:,3);
y(1) = 1.3441;
plot(x,y);
hold on;
#W = load("weidmann.dat");
plot(W(:,1),W(:,2));