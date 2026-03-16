package com.hsf.hsf302_ecom.config;

import com.hsf.hsf302_ecom.entity.*;
import com.hsf.hsf302_ecom.enums.*;
import com.hsf.hsf302_ecom.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsersRepo usersRepo;
    private final CategoriesRepo categoriesRepo;
    private final BrandsRepo brandsRepo;
    private final ProductsRepo productsRepo;
    private final ProductVariantsRepo productVariantsRepo;
    private final ProductImagesRepo productImagesRepo;
    private final InventoriesRepo inventoriesRepo;
    private final ReviewsRepo reviewsRepo;

    private static final String IMAGE_PATH = "/img/image1.webp";
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(12);
    private static final String HASHED_PASSWORD = ENCODER.encode("111111");

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (usersRepo.count() > 0) {
            log.info("Data already seeded. Skipping...");
            return;
        }

        log.info("Starting data seeding...");

        // ==============================
        // USERS
        // ==============================
        Users admin = Users.builder()
                .username("admin")
                .email("admin@ecom.com")
                .password(HASHED_PASSWORD)
                .phone("0901234567")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        Users customer1 = Users.builder()
                .username("an0other")
                .email("customer1@gmail.com")
                .password(HASHED_PASSWORD)
                .phone("0912345678")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        Users customer2 = Users.builder()
                .username("customer2")
                .email("customer2@gmail.com")
                .password(HASHED_PASSWORD)
                .phone("0923456789")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        Users customer3 = Users.builder()
                .username("customer3")
                .email("customer3@gmail.com")
                .password(HASHED_PASSWORD)
                .phone("0934567890")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        usersRepo.saveAll(List.of(admin, customer1, customer2, customer3));
        log.info("Seeded {} users (password: 111111)", usersRepo.count());

        // ==============================
        // CATEGORIES
        // ==============================
        Categories smartphones = Categories.builder().name("Smartphones").status(true).build();
        Categories laptops = Categories.builder().name("Laptops").status(true).build();
        Categories tablets = Categories.builder().name("Tablets").status(true).build();
        Categories accessories = Categories.builder().name("Accessories").status(true).build();
        Categories wearables = Categories.builder().name("Wearables").status(true).build();

        categoriesRepo.saveAll(List.of(smartphones, laptops, tablets, accessories, wearables));
        log.info("Seeded {} categories", categoriesRepo.count());

        // ==============================
        // BRANDS
        // ==============================
        Brands apple = Brands.builder().name("Apple").status(true).build();
        Brands samsung = Brands.builder().name("Samsung").status(true).build();
        Brands sony = Brands.builder().name("Sony").status(true).build();
        Brands dell = Brands.builder().name("Dell").status(true).build();
        Brands xiaomi = Brands.builder().name("Xiaomi").status(true).build();

        brandsRepo.saveAll(List.of(apple, samsung, sony, dell, xiaomi));
        log.info("Seeded {} brands", brandsRepo.count());

        // ==============================
        // PRODUCTS (20–25 rows)
        // ==============================
        List<Products> products = new ArrayList<>();

        // Smartphones - Apple
        products.add(Products.builder().name("iPhone 15 Pro").description("Latest Apple flagship smartphone with A17 Pro chip and titanium design").status(true).category(smartphones).brand(apple).build());
        products.add(Products.builder().name("iPhone 15").description("Apple iPhone 15 with Dynamic Island and 48MP camera system").status(true).category(smartphones).brand(apple).build());
        products.add(Products.builder().name("iPhone 14").description("Apple iPhone 14 with advanced dual-camera system and crash detection").status(true).category(smartphones).brand(apple).build());

        // Smartphones - Samsung
        products.add(Products.builder().name("Samsung Galaxy S24 Ultra").description("Samsung flagship with S Pen, 200MP camera, and Snapdragon 8 Gen 3").status(true).category(smartphones).brand(samsung).build());
        products.add(Products.builder().name("Samsung Galaxy S24").description("Samsung Galaxy S24 with Exynos 2400 and Galaxy AI features").status(true).category(smartphones).brand(samsung).build());
        products.add(Products.builder().name("Samsung Galaxy A55").description("Mid-range Samsung Galaxy with 50MP camera and 5000mAh battery").status(true).category(smartphones).brand(samsung).build());

        // Smartphones - Xiaomi
        products.add(Products.builder().name("Xiaomi 14 Ultra").description("Xiaomi flagship with Leica professional camera and Snapdragon 8 Gen 3").status(true).category(smartphones).brand(xiaomi).build());
        products.add(Products.builder().name("Xiaomi Redmi Note 13 Pro").description("Xiaomi Redmi Note 13 Pro with 200MP camera and 67W fast charging").status(true).category(smartphones).brand(xiaomi).build());

        // Laptops - Apple
        products.add(Products.builder().name("MacBook Pro 16 M3").description("Apple MacBook Pro 16-inch with M3 Pro chip, stunning Liquid Retina XDR display").status(true).category(laptops).brand(apple).build());
        products.add(Products.builder().name("MacBook Air M2").description("Apple MacBook Air M2 with fanless design, 18-hour battery life").status(true).category(laptops).brand(apple).build());

        // Laptops - Dell
        products.add(Products.builder().name("Dell XPS 15").description("Dell XPS 15 with Intel Core i9, OLED display and premium build quality").status(true).category(laptops).brand(dell).build());
        products.add(Products.builder().name("Dell Inspiron 15").description("Dell Inspiron 15 affordable laptop with Intel Core i7 and 512GB SSD").status(true).category(laptops).brand(dell).build());
        products.add(Products.builder().name("Dell G15 Gaming").description("Dell G15 gaming laptop with RTX 4060, 165Hz display and 16GB RAM").status(true).category(laptops).brand(dell).build());

        // Tablets - Apple
        products.add(Products.builder().name("iPad Pro 12.9 M2").description("Apple iPad Pro with M2 chip, Liquid Retina XDR display and Apple Pencil support").status(true).category(tablets).brand(apple).build());
        products.add(Products.builder().name("iPad Air M1").description("Apple iPad Air with M1 chip, 10.9-inch display and USB-C connectivity").status(true).category(tablets).brand(apple).build());

        // Tablets - Samsung
        products.add(Products.builder().name("Samsung Galaxy Tab S9").description("Samsung Galaxy Tab S9 with Snapdragon 8 Gen 2, S Pen included").status(true).category(tablets).brand(samsung).build());

        // Accessories - Sony
        products.add(Products.builder().name("Sony WH-1000XM5").description("Sony WH-1000XM5 industry-leading noise canceling wireless headphones").status(true).category(accessories).brand(sony).build());
        products.add(Products.builder().name("Sony WF-1000XM5").description("Sony WF-1000XM5 true wireless earbuds with best-in-class noise cancellation").status(true).category(accessories).brand(sony).build());

        // Accessories - Apple
        products.add(Products.builder().name("AirPods Pro 2nd Gen").description("Apple AirPods Pro 2nd generation with adaptive transparency and personalized spatial audio").status(true).category(accessories).brand(apple).build());
        products.add(Products.builder().name("Apple Watch Series 9").description("Apple Watch Series 9 with S9 chip, double tap gesture and always-on display").status(true).category(wearables).brand(apple).build());

        // Wearables - Samsung
        products.add(Products.builder().name("Samsung Galaxy Watch 6").description("Samsung Galaxy Watch 6 with advanced health monitoring and Wear OS").status(true).category(wearables).brand(samsung).build());

        // Wearables - Xiaomi
        products.add(Products.builder().name("Xiaomi Smart Band 8").description("Xiaomi Smart Band 8 with AMOLED display, 16-day battery and 150 workout modes").status(true).category(wearables).brand(xiaomi).build());

        productsRepo.saveAll(products);
        log.info("Seeded {} products", productsRepo.count());

        // ==============================
        // PRODUCT VARIANTS (2-3 per product)
        // ==============================
        List<ProductVariants> allVariants = new ArrayList<>();

        for (Products p : products) {
            String name = p.getName();

            if (name.contains("iPhone 15 Pro")) {
                allVariants.add(variant("Natural Titanium", "128GB", new BigDecimal("28990000"), p));
                allVariants.add(variant("Black Titanium",   "256GB", new BigDecimal("32990000"), p));
                allVariants.add(variant("White Titanium",  "512GB", new BigDecimal("38990000"), p));
            } else if (name.contains("iPhone 15") && !name.contains("Pro")) {
                allVariants.add(variant("Pink",   "128GB", new BigDecimal("22990000"), p));
                allVariants.add(variant("Blue",   "256GB", new BigDecimal("25990000"), p));
                allVariants.add(variant("Yellow", "512GB", new BigDecimal("30990000"), p));
            } else if (name.contains("iPhone 14")) {
                allVariants.add(variant("Midnight", "128GB", new BigDecimal("18990000"), p));
                allVariants.add(variant("Starlight","256GB", new BigDecimal("21990000"), p));
            } else if (name.contains("Galaxy S24 Ultra")) {
                allVariants.add(variant("Titanium Black",  "256GB", new BigDecimal("31990000"), p));
                allVariants.add(variant("Titanium Gray",   "512GB", new BigDecimal("36990000"), p));
                allVariants.add(variant("Titanium Yellow", "1TB",   new BigDecimal("44990000"), p));
            } else if (name.contains("Galaxy S24") && !name.contains("Ultra")) {
                allVariants.add(variant("Onyx Black", "128GB", new BigDecimal("18990000"), p));
                allVariants.add(variant("Marble Gray","256GB", new BigDecimal("21990000"), p));
            } else if (name.contains("Galaxy A55")) {
                allVariants.add(variant("Awesome Iceblue",  "128GB", new BigDecimal("9990000"), p));
                allVariants.add(variant("Awesome Navy",     "256GB", new BigDecimal("11990000"), p));
            } else if (name.contains("Xiaomi 14 Ultra")) {
                allVariants.add(variant("Black",  "256GB", new BigDecimal("26990000"), p));
                allVariants.add(variant("White",  "512GB", new BigDecimal("30990000"), p));
            } else if (name.contains("Redmi Note 13 Pro")) {
                allVariants.add(variant("Midnight Black", "128GB", new BigDecimal("7490000"), p));
                allVariants.add(variant("Midnight Black", "256GB", new BigDecimal("7490000"), p));
                allVariants.add(variant("Aurora Purple", "256GB", new BigDecimal("8990000"), p));
                allVariants.add(variant("Coral Pink",    "256GB", new BigDecimal("8990000"), p));
            } else if (name.contains("MacBook Pro 16")) {
                allVariants.add(variant("Space Black",  "M3 Pro / 18GB / 512GB SSD",  new BigDecimal("62990000"), p));
                allVariants.add(variant("Silver",       "M3 Pro / 36GB / 1TB SSD",    new BigDecimal("79990000"), p));
            } else if (name.contains("MacBook Air M2")) {
                allVariants.add(variant("Midnight",   "M2 / 8GB / 256GB SSD",  new BigDecimal("26990000"), p));
                allVariants.add(variant("Starlight",  "M2 / 16GB / 512GB SSD", new BigDecimal("34990000"), p));
                allVariants.add(variant("Space Gray", "M2 / 24GB / 1TB SSD",   new BigDecimal("41990000"), p));
            } else if (name.contains("Dell XPS 15")) {
                allVariants.add(variant("Platinum Silver", "i7 / 16GB / 512GB",  new BigDecimal("45990000"), p));
                allVariants.add(variant("Platinum Silver", "i9 / 32GB / 1TB",    new BigDecimal("59990000"), p));
            } else if (name.contains("Dell Inspiron 15")) {
                allVariants.add(variant("Platinum Silver", "i5 / 8GB / 256GB",  new BigDecimal("15990000"), p));
                allVariants.add(variant("Carbon Black",    "i7 / 16GB / 512GB", new BigDecimal("19990000"), p));
                allVariants.add(variant("Platinum Silver", "i7 / 32GB / 1TB",   new BigDecimal("24990000"), p));
            } else if (name.contains("Dell G15")) {
                allVariants.add(variant("Specter Green", "RTX 4060 / 16GB / 512GB", new BigDecimal("29990000"), p));
                allVariants.add(variant("Specter Green", "RTX 4070 / 32GB / 1TB",   new BigDecimal("38990000"), p));
            } else if (name.contains("iPad Pro")) {
                allVariants.add(variant("Space Gray", "128GB WiFi",          new BigDecimal("28990000"), p));
                allVariants.add(variant("Silver",     "256GB WiFi",          new BigDecimal("33990000"), p));
                allVariants.add(variant("Space Gray", "128GB WiFi+Cellular", new BigDecimal("36990000"), p));
            } else if (name.contains("iPad Air")) {
                allVariants.add(variant("Blue",      "64GB WiFi",  new BigDecimal("16990000"), p));
                allVariants.add(variant("Starlight", "256GB WiFi", new BigDecimal("22990000"), p));
            } else if (name.contains("Galaxy Tab S9")) {
                allVariants.add(variant("Graphite", "128GB WiFi",    new BigDecimal("19990000"), p));
                allVariants.add(variant("Beige",    "256GB WiFi",    new BigDecimal("23990000"), p));
                allVariants.add(variant("Graphite", "256GB WiFi+5G", new BigDecimal("27990000"), p));
            } else if (name.contains("WH-1000XM5")) {
                allVariants.add(variant("Black",  "Standard", new BigDecimal("8490000"), p));
                allVariants.add(variant("Silver", "Standard", new BigDecimal("8490000"), p));
            } else if (name.contains("WF-1000XM5")) {
                allVariants.add(variant("Black",  "Standard", new BigDecimal("6990000"), p));
                allVariants.add(variant("Silver", "Standard", new BigDecimal("6990000"), p));
            } else if (name.contains("AirPods Pro")) {
                allVariants.add(variant("White", "MagSafe Charging Case", new BigDecimal("6490000"), p));
                allVariants.add(variant("White", "USB-C Charging Case",   new BigDecimal("6490000"), p));
            } else if (name.contains("Apple Watch Series 9")) {
                allVariants.add(variant("Midnight", "41mm GPS",          new BigDecimal("10990000"), p));
                allVariants.add(variant("Starlight","45mm GPS",          new BigDecimal("11990000"), p));
                allVariants.add(variant("Red",      "45mm GPS+Cellular", new BigDecimal("13990000"), p));
            } else if (name.contains("Galaxy Watch 6")) {
                allVariants.add(variant("Graphite", "40mm", new BigDecimal("6990000"), p));
                allVariants.add(variant("Gold",     "44mm", new BigDecimal("7990000"), p));
            } else if (name.contains("Smart Band 8")) {
                allVariants.add(variant("Black",  "Standard", new BigDecimal("990000"), p));
                allVariants.add(variant("White",  "Standard", new BigDecimal("990000"), p));
                allVariants.add(variant("Orange", "Standard", new BigDecimal("990000"), p));
            } else {
                allVariants.add(variant("Black", "Standard", new BigDecimal("9990000"), p));
                allVariants.add(variant("White", "Standard", new BigDecimal("9990000"), p));
            }
        }

        productVariantsRepo.saveAll(allVariants);
        log.info("Seeded {} product variants", productVariantsRepo.count());

        // ==============================
        // PRODUCT IMAGES (3 per product)
        // ==============================
        List<ProductImages> allImages = new ArrayList<>();
        for (Products p : products) {
            allImages.add(ProductImages.builder().imageUrl(IMAGE_PATH).isPrimary(true).product(p).build());
            allImages.add(ProductImages.builder().imageUrl(IMAGE_PATH).isPrimary(false).product(p).build());
            allImages.add(ProductImages.builder().imageUrl(IMAGE_PATH).isPrimary(false).product(p).build());
        }
        productImagesRepo.saveAll(allImages);
        log.info("Seeded {} product images", productImagesRepo.count());

        // ==============================
        // INVENTORIES (one per variant)
        // ==============================
        List<Inventories> allInventories = new ArrayList<>();
        for (ProductVariants v : allVariants) {
            allInventories.add(Inventories.builder()
                    .stock(3L /*+ (long)(Math.random() * 150)*/)
                    .reserved(0L)
                    .productVariant(v)
                    .build());
        }
        inventoriesRepo.saveAll(allInventories);
        log.info("Seeded {} inventories", inventoriesRepo.count());

        // ==============================
        // REVIEWS
        // ==============================
        List<Reviews> allReviews = new ArrayList<>();
        List<Users> customers = List.of(customer1, customer2, customer3);

        String[][] reviewContents = {
                {"5", "Absolutely love this product! Exceeded all my expectations."},
                {"5", "Amazing quality and fast delivery. Would definitely buy again."},
                {"4", "Very good product, works exactly as described. Happy with the purchase."},
                {"4", "Great value for money. Build quality is solid and performance is smooth."},
                {"3", "Decent product but packaging could be improved. Overall satisfied."},
                {"5", "Outstanding performance! This is my second purchase and still impressed."},
                {"4", "Good product overall. Setup was easy and works flawlessly so far."},
                {"3", "Average experience. Product is fine but nothing extraordinary."},
                {"5", "Top-notch quality! Highly recommend to anyone looking for reliability."},
                {"4", "Really satisfied with this purchase. Shipping was quick too."},
                {"5", "Best purchase I've made this year! Performance is exceptional."},
                {"4", "Solid build quality and responsive. Worth every penny."},
        };

        for (int i = 0; i < products.size(); i++) {
            Products product  = products.get(i);
            Users    reviewer = customers.get(i % customers.size());
            String[] content  = reviewContents[i % reviewContents.length];

            allReviews.add(Reviews.builder()
                    .rating(Byte.parseByte(content[0]))
                    .comment(content[1])
                    .user(reviewer)
                    .product(product)
                    .build());
        }

        reviewsRepo.saveAll(allReviews);
        log.info("Seeded {} reviews", reviewsRepo.count());

        log.info("Data seeding completed successfully! All accounts use password: 111111");
    }

    private ProductVariants variant(String color, String spec, BigDecimal price, Products product) {
        return ProductVariants.builder()
                .color(color)
                .spec(spec)
                .price(price)
                .status(true)
                .product(product)
                .build();
    }
}