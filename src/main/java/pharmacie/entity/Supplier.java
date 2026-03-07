package pharmacie.entity;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor @ToString
public class Supplier {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(AccessLevel.NONE) // la clé est auto-générée par la BD
	private Integer id;

	@NonNull
	@Size(min = 1, max = 255)
	@Column(unique = false, length = 255)
	@NotBlank
	private String nom;

	@NonNull
	@Email
	@Size(min = 1, max = 255)
	@Column(unique = true, length = 255)
	@NotBlank
	private String email;

	@ToString.Exclude
	// Relation many-to-many avec les catégories
	@ManyToMany
	@JoinTable(
		name = "SUPPLIER_CATEGORIE",
		joinColumns = @JoinColumn(name = "supplier_id"),
		inverseJoinColumns = @JoinColumn(name = "categorie_code")
	)
	@JsonIgnoreProperties("suppliers")
	private List<Categorie> categories = new LinkedList<>();

}
