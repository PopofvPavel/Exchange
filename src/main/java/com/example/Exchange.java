package com.example;

import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Exchange {
    private static final Valuta[] valutas = Valuta.values();
    private static final List<Client> clients = new ArrayList<>();
    private static final List<Order> orders = new ArrayList<>();
    public static final List<CurrencyPair> currencyPairs = new ArrayList<>();

    private static int processedOrdersCounter = 0;
    private static int matchCounter = 0;

    static {
        //Купить - значит получить base за quote, продать - получить quote за base

        currencyPairs.add(new CurrencyPair(Valuta.USD, Valuta.EUR, 0.85));
        currencyPairs.add(new CurrencyPair(Valuta.USD, Valuta.RUB, 90.0));
        currencyPairs.add(new CurrencyPair(Valuta.EUR, Valuta.RUB, 100.0));
        currencyPairs.add(new CurrencyPair(Valuta.BYN, Valuta.RUB, 33.0));

    }

    public static String getOrdersString() {
        String result = "Список orders:\n";
        for (Order order : orders) {
            result += order.toString();
        }
        return result;
    }

    public static Order getOrderFromList(int idClient) {
        for (Order order : orders) {
            if (order.getClientId() == idClient) {
                return order;
            }
        }
        return null;
    }
    public static void createClient(int id) {
        // Проверить, есть ли уже клиент с таким id
        if (isClientIdUnique(id)) {
            Client client = new Client(id);
            clients.add(client);
        } else {
            throw new IllegalArgumentException("Такой id же существует");
        }
    }

    private static boolean isClientIdUnique(int id) {
        for (Client client : clients) {
            if (client.getId() == id) {
                return false; // Клиент с таким id уже существует
            }
        }
        return true; // Клиент с таким id не существует
    }

    public static String getClientBalance(int id) {
        for (Client client : clients) {
            if (client.getId() == id) {
                return client.getFullBalance(); // Клиент с таким id уже существует
            }
        }
        return "Данный клиент не найден: " + id;

    }

    public static double getClientBalanceInValute(int id, Valuta valuta) {
        for (Client client : clients) {
            if (client.getId() == id) {
                return client.getBalanceOfValuta(valuta); // Клиент с таким id уже существует
            }
        }
        return 0.0;
    }

    public static Client getClientFromList(int id) {
        for (Client client : clients) {
            if (client.getId() == id) {
                return client;
            }
        }
        return null;
    }

    public static void addBalanceToClient(int idClient, Valuta valuta, double money) throws Exception {
        Client client = getClientFromList(idClient);
        if (client != null) {
            client.addBalance(valuta, money);
        } else {
            throw new Exception("Данный клиент не найден");
        }

    }

    public static void reduceBalanceToClient(int idClient, Valuta valuta, double moneyReduce) throws Exception {
        Client client = getClientFromList(idClient);
        if (client != null) {
            client.reduceBalance(valuta, moneyReduce);
        } else {
            throw new Exception("Данный клиент не найден");
        }
    }

    public static void createOrder(int clientId, Valuta baseCurrency, Valuta quoteCurrency, Type type, double price, double volume) throws Exception {
        CurrencyPair pair = getCurrencyPair(baseCurrency, quoteCurrency);
        if (pair != null) {
            checkLimits(baseCurrency, quoteCurrency, clientId, type, price, volume);
            Order order = new Order(clientId, pair, type, price, volume);
            orders.add(order);
        } else {
            throw new Exception("Такая валютная пара не существует.");
        }
    }

    private static void checkLimits(Valuta baseCurrency, Valuta quoteCurrency, int clientId, Type type, double price, double volume) throws Exception {
        Client client = getClientFromList(clientId);
        if (volume == 0.0) {
            throw new Exception("Нельзя создать заявку на 0 покупки или продажи");
        }
        if (client == null) {
            throw new Exception("Данный клиент не найден");
        }
        if (type == Type.BUY) {
            if (client.getBalanceOfValuta(quoteCurrency) < price * volume) {
                throw new Exception("У клиента недостаточно денег для покупки:\n"
                        + "Чтобы купить " + volume + " " + baseCurrency.name() + " необходимо " + price * volume
                        + " " + quoteCurrency.name() + "\n" + "Доступно: " + client.getBalanceOfValuta(quoteCurrency) + quoteCurrency.name()
                );
            }
        } else if (type == Type.SELL) {
            if (client.getBalanceOfValuta(baseCurrency) < volume) {
                throw new Exception("У клиента недостаточно денег для продажи:\n"
                        + "Чтобы продать " + volume + " " + baseCurrency.name()
                        + "\n" + "Доступно: " + client.getBalanceOfValuta(baseCurrency) + baseCurrency.name()
                );
            }
        }

        double exchangeRate = getCurrencyPair(baseCurrency, quoteCurrency).getExchangeRate();
        System.out.println("rate = " + exchangeRate);
        double upperLimit = 1.1 * exchangeRate;
        System.out.println("upperLimit" + upperLimit);
        double lowerLimit = 0.9 * exchangeRate;
        if (price > upperLimit) {
            throw new Exception("Слишком высокая цена, выход за лимит: " + upperLimit);
        }
        if (price < lowerLimit) {
            throw new Exception("Слишком низкая цена, выход за лимит" + lowerLimit);
        }


    }

    private static CurrencyPair getCurrencyPair(Valuta baseCurrency, Valuta quoteCurrency) {
        for (CurrencyPair currencyPair : currencyPairs) {
            if (currencyPair.getBaseCurrency().equals(baseCurrency) && currencyPair.getQuoteCurrency().equals(quoteCurrency)) {
                return currencyPair;
            }
        }
        return null;
    }

    public static synchronized void processOrders() throws Exception {
        List<Order> processedOrders = new ArrayList<>();

        for (Order buyOrder : orders) {
            List<Order> matchingSellOrders = findMatchingSellOrders(buyOrder);

            for (Order sellOrder : matchingSellOrders) {
                matchCounter++;
                executeTrade(buyOrder, sellOrder);
                if (sellOrder.getVolume() <= 0) {
                    processedOrders.add(sellOrder);
                }
            }

            if (buyOrder.getVolume() <= 0) {
                processedOrders.add(buyOrder);
            }

            System.out.println("Конец обработки заявки для клиента " + buyOrder.getClientId());
        }

        // Удаление обработанных заявок
        orders.removeAll(processedOrders);
    }

    private static List<Order> findMatchingSellOrders(Order buyOrder) {
        List<Order> matchingSellOrders = new ArrayList<>();

        for (Order sellOrder : orders) {
            if (isMatch(buyOrder, sellOrder)) {
                matchingSellOrders.add(sellOrder);
            }
        }

        return matchingSellOrders;
    }



    public static boolean isMatch(Order buyOrder, Order sellOrder) {
        // Проверка условий для сопоставления заявок
        return buyOrder.getCurrencyPair().equals(sellOrder.getCurrencyPair()) &&  // Проверка валютной пары
                buyOrder.getType() == Type.BUY && sellOrder.getType() == Type.SELL &&  // Проверка типов заявок
                (Math.abs(sellOrder.getPrice() - buyOrder.getPrice()) <= 0.1 * buyOrder.getVolume() * buyOrder.getCurrencyPair().getExchangeRate());
        // buyOrder.getPrice() >= sellOrder.getPrice();  // Проверка цены
    }


    private static void executeTrade(Order buyOrder, Order sellOrder) throws Exception {
        // Выполнение сделки и обновление балансов клиентов
        double amount = Math.min(buyOrder.getVolume(), sellOrder.getVolume());
        double totalPrice = amount * sellOrder.getPrice();

        if (amount > 0) {  // Проверка на ненулевой объем перед выполнением сделки
            // Обновление балансов
            Client buyer = getClientFromList(buyOrder.getClientId());
            buyer.addBalance(buyOrder.getCurrencyPair().getBaseCurrency(), amount);
            buyer.reduceBalance(buyOrder.getCurrencyPair().getQuoteCurrency(), totalPrice);

            Client seller = getClientFromList(sellOrder.getClientId());
            seller.addBalance(sellOrder.getCurrencyPair().getQuoteCurrency(), totalPrice);
            seller.reduceBalance(sellOrder.getCurrencyPair().getBaseCurrency(), amount);
            System.out.println("Провели заявку на " + amount + buyOrder.getCurrencyPair().getBaseCurrency().name());
            processedOrdersCounter++;
            // Уменьшение объема заявок после сделки
            buyOrder.reduceVolume(amount);
            sellOrder.reduceVolume(amount);
        }
        // Обновление балансов
     /*   Client buyer = getClientFromList(buyOrder.getClientId());
        buyer.addBalance(buyOrder.getCurrencyPair().getBaseCurrency(), amount);
        buyer.reduceBalance(buyOrder.getCurrencyPair().getQuoteCurrency(), totalPrice);

        Client seller = getClientFromList(sellOrder.getClientId());
        seller.addBalance(sellOrder.getCurrencyPair().getQuoteCurrency(), totalPrice);
        seller.reduceBalance(sellOrder.getCurrencyPair().getBaseCurrency(), amount);

        // Уменьшение объема заявок после сделки
        System.out.println("Провели заявку на " + amount + buyOrder.getCurrencyPair().getBaseCurrency().name());
        processedOrdersCounter++;
        buyOrder.reduceVolume(amount);
        sellOrder.reduceVolume(amount);*/

    }

    public static int getProcessedOrdersCounter() {
        return processedOrdersCounter;
    }

    public static int getMatchCounter() {
        return matchCounter;
    }
}
