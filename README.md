# Bottle Crate Store

**Assignment 1: Beverage Store**

- Application runs on port 8080 ([http://localhost:8080](http://localhost:8080))

- **Application Flow:**

  - In First page, all beverages (bottles and crates) are listed.
  - User can choose the quantity (>0) and click on "Add To Cart".
  - "Shopping Cart" link on top-right will be updated with the number of items user selects.
  - Clicking on "Shopping Cart" link, user can review, remove or update his selected items in shopping Cart, or he can go back to continue shopping.
  - In the next page, user can view and update his saved billing address and delivery address.
  - After saving the address, the order will take place and the "checkout" page will be displayed with order details.
  - User is also able to add new bottle or new crate, using the links on top of the page.
  - To see the order history, user can click on "order list" link on top of the page. doing do, the list of orders will be displayed and user can see details for his selected order.

- **Domain Model:**

* We used the domain model defined in assignment sheet. with slight changes:
  - **id**, **price**, **name** and **pic** are stored in Beverage , as both Bottle and Crate entities share these attributes.
  - new attribute **quantity** is added to OrderItem.

**Assignment 2: Security, Google Cloud, Cloud Functions and Ops**

**Default User:**

- **Username:** Max
- **Password:** 123456

**Admin user:**

- **Username:** Admin
- **Password:** 123456

- To Run in Local

1. To run Main Controller -
   - Go to group 15 gradle task -> assignment1 -> Tasks -> application -> click on bootRun.
2. To run pdf generation function -
   - Go to group 15 gradle task -> pdf-generation-function -> Tasks -> other -> click on runFunction.

Access the application at: http://localhost:8080/

Login with Default user or create new user. Please provide valid email id to verify mail sending function.
Place an order and checkout. PDF should be saved on cloud storage bucket and email should be triggered. You should receive PDF in the email provided.

- To Run in cloud, URL is -

http://beverage-store-group15.dt.r.appspot.com/

Login with Default user or create new user. Please provide valid email id to verify mail sending function.
Place an order and checkout. PDF should be saved on cloud storage bucket and email should be triggered. You should receive PDF in the email provided.
