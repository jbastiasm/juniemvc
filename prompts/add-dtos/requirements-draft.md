Update the BeerController to use DTOs. In the package `m̀odels`, create a new POJO called BeerDTO with the same 
properties as the JPA Entity Beer. The DTO should be use annotations from Project Lombok, including Builder. Create
a Mapstruct mapper to convert to and from the DTO. Mappers should be added to the package `mappers`. When converting from 
 a DTO to the JPA Entity ignore the properties for id, createDate and updateDate. Convert the service layer to accept DTO
objects and to use Mapstruct mapper for the conversion. Update the controller methods to use the new DTO pojo for input and
access to service methods.